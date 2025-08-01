package com.ticketmate.backend.concert.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConcertAgentAvailability extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID concertAgentOptionId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Concert concert;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member agent;

  @Builder.Default
  private boolean accepting = true;

  private String introduction;

  public void updateAcceptingStatus(boolean accepting, String introduction) {
    this.accepting = accepting;
    this.introduction = accepting ? introduction : null;
  }
}
