package com.ticketmate.backend.search.infrastructure.service;

import com.ticketmate.backend.search.infrastructure.properties.SearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentSearchService {
  private final RedisTemplate<String, String> redisTemplate;
  private final SearchProperties searchProperties;
  private static final String RECENT_SEARCH_KEY = "searches:recent:";

  /**
   * 검색어 추가
   */
  public void addRecentSearch(UUID memberId, String keyword){
    String key = buildRecentSearchKey(memberId);
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

    // ZSET에 (keyword, timestamp) 추가
    zSetOperations.add(key, keyword, System.currentTimeMillis());

    // 가장 오래된 데이터 삭제 (최대 개수 초과 시)
    int max = searchProperties.recent().maxSize();
    Long size = zSetOperations.size(key);
    if(size != null && size > max){
      long removeCount = size - max;
      zSetOperations.remove(key, 0, removeCount - 1);
      log.debug("새 검색어 '{}' 추가로 최대 개수({}) 초과. 가장 오래된 {}개를 삭제합니다. (memberId: {})", keyword, max, removeCount, memberId);
    }

    // ttl 설정
    redisTemplate.expire(key, searchProperties.recent().ttlDays(), TimeUnit.DAYS);
  }

  /**
   * 최근 검색어 조회
   */
  public List<String> getRecentSearches(UUID memberId){
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

    Set<String> keywords = zSetOperations.reverseRange(buildRecentSearchKey(memberId), 0, searchProperties.recent().maxSize() - 1);
    List<String> result = new ArrayList<>(keywords != null ? keywords : Collections.emptyList());
    log.debug("memberId: {}의 최근 검색어 {}건을 조회했습니다.", memberId, result.size());
    return result;
  }

  /**
   * 최근 검색어 전체 삭제
   */
  public void deleteAllRecentSearches(UUID memberId){
    redisTemplate.delete(buildRecentSearchKey(memberId));
    log.debug("memberId: {}의 최근 검색어를 전체 삭제했습니다.", memberId);
  }

  private String buildRecentSearchKey(UUID memberId){
    return RECENT_SEARCH_KEY + memberId.toString();
  }
}

