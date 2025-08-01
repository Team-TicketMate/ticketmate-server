package com.ticketmate.backend.member.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
  @Column(nullable = false)
  private double averageRating;

  // 전체 후기 수
  @Column(nullable = false)
  private int reviewCount;

  // 최근 30일 성공 수
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
}
