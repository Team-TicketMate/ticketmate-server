package com.ticketmate.backend.member.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agent.performance.score")
@Getter
@Setter
public class AgentPerformanceScoreConfig {

  // 리뷰 품질 점수 배수
  private double reviewMultiplier = 10.0;

  // 팔로워 로그 스케일 배수
  private double followerMultiplier = 3.0;

  // 평균 별점 점수 배수
  private double ratingMultiplier = 5.0;

  // 최근 30일 성공 수 점수 배수
  private double successMultiplier = 2.5;

  // 리뷰 품질 기준점
  private double reviewQualityThreshold = 3.0;
}