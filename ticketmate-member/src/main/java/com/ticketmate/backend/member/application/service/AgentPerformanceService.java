package com.ticketmate.backend.member.application.service;

import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.AgentPerformanceSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgentPerformanceService {
  private final AgentPerformanceSummaryRepository agentPerformanceSummaryRepository;

  /**
   * 리뷰 생성 시 통계 업데이트
   */
  @Transactional
  public void addReviewStats(Member agent, double rating) {
    AgentPerformanceSummary summary = findSummary(agent);

    summary.updateAverageRating(true, rating);
  }

  /**
   * 리뷰 삭제 시 통계 업데이트
   */
  @Transactional
  public void deleteReviewStats(Member agent, double rating) {
    AgentPerformanceSummary summary = findSummary(agent);

    summary.updateAverageRating(false, -rating);
  }

  /**
   * 리뷰 수정 시 통계 업데이트
   */
  @Transactional
  public void updateReviewStats(Member agent, double oldRating, double newRating) {
    AgentPerformanceSummary summary = findSummary(agent);

    double ratingDiff = newRating - oldRating;
    summary.updateAverageRating(ratingDiff);
  }

  /**
   * 요약 정보 조회
   */
  private AgentPerformanceSummary findSummary(Member agent) {
    // PESSIMISTIC_WRITE 락으로 동시성 문제 방지
    return agentPerformanceSummaryRepository.findByAgentId(agent.getMemberId());
  }
}
