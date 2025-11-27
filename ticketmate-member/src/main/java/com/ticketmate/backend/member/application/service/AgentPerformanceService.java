package com.ticketmate.backend.member.application.service;

import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.AgentPerformanceSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentPerformanceService {
  private final AgentPerformanceSummaryRepository agentPerformanceSummaryRepository;

  /**
   * 리뷰 생성 시 통계 업데이트
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void addReviewStats(Member agent, double rating) {
    AgentPerformanceSummary summary = findSummary(agent);
    summary.updateAverageRating(true, rating);

    log.debug("리뷰 생성으로 대리인(ID: {})에게 별점 {}점이 추가되었습니다.", agent.getMemberId(), rating);
  }

  /**
   * 리뷰 삭제 시 통계 업데이트
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteReviewStats(Member agent, double rating) {
    AgentPerformanceSummary summary = findSummary(agent);
    summary.updateAverageRating(false, rating);

    log.debug("리뷰 삭제로 대리인(ID: {})에게 별점 {}점이 제거되었습니다.", agent.getMemberId(), rating);
  }

  /**
   * 리뷰 수정 시 통계 업데이트
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateReviewStats(Member agent, double oldRating, double newRating) {
    AgentPerformanceSummary summary = findSummary(agent);
    double ratingDiff = newRating - oldRating;
    summary.updateAverageRating(ratingDiff);

    log.debug("리뷰 수정으로 대리인(ID: {})의 별점이 {}점에서 {}점으로 변경되었습니다.",
        agent.getMemberId(), oldRating, newRating);
  }

  /**
   * 요약 정보 조회
   */
  private AgentPerformanceSummary findSummary(Member agent) {
    // PESSIMISTIC_WRITE 락으로 동시성 문제 방지
    return agentPerformanceSummaryRepository.findByAgentId(agent.getMemberId());
  }
}
