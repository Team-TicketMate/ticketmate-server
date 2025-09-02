package com.ticketmate.backend.search.infrastructure.repository;

import com.ticketmate.backend.search.application.dto.view.AgentSearchInfo;
import com.ticketmate.backend.search.application.dto.view.ConcertSearchInfo;
import java.util.List;
import java.util.UUID;

public interface SearchRepositoryCustom {

  /**
   * ID 리스트 기반 ConcertSearchInfo DTO 리스트 반환
   */
  public List<ConcertSearchInfo> findConcertDetailsByIds(List<UUID> concertIds);

  /**
   * 공연 키워드 LIKE 검색
   * 공연명, 공연 카테고리, 공연장 기준 비교
   */
  List<UUID> findConcertIdsByKeyword(String keyword, int limit);

  /**
   * ID 리스트 기반 AgentSearchInfo DTO 리스트 반환
   */
  public List<AgentSearchInfo> findAgentDetailsByIds(List<UUID> agentIds);

  /**
   * 대리인 키워드 LIKE 검색
   * 닉네임, 한줄 소개 기준 비교
   */
  public List<UUID> findAgentIdsByKeyword(String keyword, int limit);
}
