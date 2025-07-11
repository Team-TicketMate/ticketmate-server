package com.ticketmate.backend.domain.member.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.global.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
  @Column(name = "agent_id")
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

  // 전체 팔로워 수
  @Column(nullable = false)
  private int followerCount;

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
