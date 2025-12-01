package com.ticketmate.backend.member.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "agent.performance.score")
public record AgentPerformanceScoreProperties(
  // 리뷰 품질 점수 배수
  double reviewMultiplier,

  // 팔로워 로그 스케일 배수
  double followerMultiplier,

  // 평균 별점 점수 배수
  double ratingMultiplier,

  // 최근 30일 성공 수 점수 배수
  double successMultiplier,

  // 리뷰 품질 기준점
  double reviewQualityThreshold
) {
  public AgentPerformanceScoreProperties(
    @DefaultValue("10.0") double reviewMultiplier,
    @DefaultValue("3.0") double followerMultiplier,
    @DefaultValue("5.0") double ratingMultiplier,
    @DefaultValue("2.5") double successMultiplier,
    @DefaultValue("3.0") double reviewQualityThreshold) {

    this.reviewMultiplier = reviewMultiplier;
    this.followerMultiplier = followerMultiplier;
    this.ratingMultiplier = ratingMultiplier;
    this.successMultiplier = successMultiplier;
    this.reviewQualityThreshold = reviewQualityThreshold;
  }
}