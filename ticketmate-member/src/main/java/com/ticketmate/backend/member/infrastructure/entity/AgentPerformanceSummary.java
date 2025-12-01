package com.ticketmate.backend.member.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AgentPerformanceSummary extends BasePostgresEntity implements Persistable<UUID> {

  @Id
  private UUID agentId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  private Member agent;

  // AI 추천 총점
  @Column(nullable = false)
  private double totalScore;

  // 평균 별점
  @DecimalMin(value = "0.0")
  @DecimalMax(value = "5.0")
  @Column(nullable = false)
  private double averageRating;

  // 전체 후기 수
  @PositiveOrZero
  @Column(nullable = false)
  private int reviewCount;

  // 전체 별점 총합
  @Column(nullable = false)
  private double totalRatingSum;

  // 최근 30일 성공 수
  @PositiveOrZero
  @Column(nullable = false)
  private int recentSuccessCount;

  @Override
  public UUID getId() {
    return this.agentId;
  }

  @Override
  public boolean isNew() {
    return getCreatedDate() == null;
  }

  // 후기 평균 업데이트
  public void updateAverageRating(boolean plus, double rating) {
    this.reviewCount += plus ? 1 : -1;
    if (this.reviewCount == 0) {
      this.totalRatingSum = 0;
      this.averageRating = 0;
      return;
    }
    this.totalRatingSum += plus ? rating : -rating;
    this.averageRating = totalRatingSum / reviewCount;
  }

  // 후기 수정 시 업데이트
  public void updateAverageRating(double ratingDiff) {
    if (this.reviewCount == 0) {
      this.averageRating = 0;
      return;
    }
    this.totalRatingSum += ratingDiff;
    this.averageRating = totalRatingSum / reviewCount;
  }

  // 최근 30일 성공 수 업데이트
  public void updateRecentSuccessCount() {
    this.recentSuccessCount++;
  }
}
