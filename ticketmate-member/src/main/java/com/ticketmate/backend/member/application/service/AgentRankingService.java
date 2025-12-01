package com.ticketmate.backend.member.application.service;

import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.properties.AgentPerformanceScoreProperties;
import com.ticketmate.backend.member.infrastructure.repository.AgentPerformanceSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(AgentPerformanceScoreProperties.class)
public class AgentRankingService {
  private final AgentPerformanceSummaryRepository agentPerformanceSummaryRepository;
  private final AgentPerformanceScoreProperties config;

  /**
   * 대리인 총점 업데이트
   */
  @Transactional
  public void updateTotalScoreRanking() {
    log.debug("대리인 총점 업데이트를 시작합니다.");

    List<AgentPerformanceSummary> summaries = agentPerformanceSummaryRepository.findAll();

    if (summaries.isEmpty()) {
      log.warn("대리인 총점 계산 중 업데이트할 대리인 데이터가 없습니다.");
      return;
    }

    summaries.forEach(summary -> {
      double score = calculateTotalScore(summary);

      // DB에 totalScore 반영
      summary.setTotalScore(score);
    });

    log.debug("총 {}명의 대리인 총점이 갱신되었습니다.", summaries.size());
  }

  /**
   * 대리인 총점 점수 계산
   * - 리뷰: 품질 기반(품질 점수 * sqrt(리뷰수))
   * - 팔로워: log(1+팔로워수)
   * - 별점: 가중치
   * - 최근 성공 수: 가중치
   */
  private double calculateTotalScore(AgentPerformanceSummary s) {
    double avgRating = s.getAverageRating();

    // 리뷰 품질 점수 (-1.0 ~ +1.0)
    // 낮은 리뷰는 낮은 score로 이어지게 하기 위함
    double reviewQuality = (avgRating - config.reviewQualityThreshold()) / 2.0;
    double reviewScore = Math.sqrt(s.getReviewCount()) * reviewQuality * config.reviewMultiplier();

    // 팔로워 점수 - log 스케일
    double followerScore = Math.log(s.getAgent().getFollowerCount() + 1) * config.followerMultiplier();

    // 평균 별점 점수
    double ratingScore = avgRating * config.ratingMultiplier();

    // 최근 30일 성공 수 점수
    double successScore = s.getRecentSuccessCount() * config.successMultiplier();

    double totalScore = reviewScore + followerScore + ratingScore + successScore;

    log.debug(
      "Agent({}) : review:{}, follower:{}, rating:{}, success:{} - total:{}",
      s.getAgent().getMemberId(),
      reviewScore,
      followerScore,
      ratingScore,
      successScore,
      totalScore
    );

    return totalScore;
  }
}
