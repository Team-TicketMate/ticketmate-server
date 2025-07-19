package com.ticketmate.backend.domain.member.repository;

import com.ticketmate.backend.domain.search.domain.dto.response.AgentSearchResponse;
import java.util.List;
import java.util.UUID;

public interface MemberRepositoryCustom {
  /**
   * 대리인 키워드 LIKE 검색
   * 닉네임, 한줄 소개 기준 비교
   */
  public List<UUID> findAgentIdsByKeyword(String keyword);

  /**
   * ID 리스트 기반 AgentSearchResponse DTO 리스트 반환
   */
  public List<AgentSearchResponse> findAgentDetailsByIds(List<UUID> agentIds);
}
