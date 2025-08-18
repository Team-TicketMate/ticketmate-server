package com.ticketmate.backend.search.infrastructure.service;

import com.ticketmate.backend.search.infrastructure.properties.SearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RecentSearchService {
  private final RedisTemplate<String, String> redisTemplate;
  private final SearchProperties searchProperties;
  private static final String RECENT_SEARCH_KEY = "searches:recent:";

  private String key(UUID memberId){
    return RECENT_SEARCH_KEY + memberId.toString();
  }

  /**
   * 검색어 추가
   */
  public void addRecentSearch(UUID memberId, String keyword){
    String key = key(memberId);
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

    // ZSET에 (keyword, timestamp) 추가
    zSetOperations.add(key, keyword, System.currentTimeMillis());

    // 가장 오래된 데이터 삭제 (최대 개수 초과 시)
    zSetOperations.removeRange(key, 0, -(searchProperties.recent().maxSize() + 1));

    // ttl 설정
    redisTemplate.expire(key, searchProperties.recent().ttlDays(), TimeUnit.DAYS);
  }

  /**
   * 최근 검색어 조회
   */
  public List<String> getRecentSearches(UUID memberId){
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

    Set<String> keywords = zSetOperations.reverseRange(key(memberId), 0, searchProperties.recent().maxSize() - 1);
    return new ArrayList<>(keywords != null ? keywords : Collections.emptyList());
  }

  /**
   * 최근 검색어 전체 삭제
   */
  public void deleteAllRecentSearches(UUID memberId){
      redisTemplate.delete(key(memberId));
  }
}

