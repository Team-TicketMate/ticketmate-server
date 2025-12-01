package com.ticketmate.backend.member.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "agent.performance.score")
public record AgentPerformanceScoreProperties(
  // 리뷰 품질 점수 배수
  @DefaultValue("10.0") double reviewMultiplier,
  // 팔로워 로그 스케일 배수
  @DefaultValue("3.0") double followerMultiplier,
  // 평균 별점 점수 배수
  @DefaultValue("5.0") double ratingMultiplier,
  // 최근 30일 성공 수 점수 배수
  @DefaultValue("2.5") double successMultiplier,
  // 리뷰 품질 기준점
  @DefaultValue("3.0") double reviewQualityThreshold
) {}