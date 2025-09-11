package com.ticketmate.backend.concert.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class ConcertDate extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID concertDateId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Concert concert; // 공연

  @Column(nullable = false, columnDefinition = "TIMESTAMPTZ(0)")
  private Instant performanceDate; // 공연 일자

  @Column(nullable = false)
  @Builder.Default
  private int session = 1; // 회차

  public static ConcertDate of(Concert concert, Instant performanceDate, int session) {
    return ConcertDate.builder()
        .concert(concert)
        .performanceDate(performanceDate)
        .session(session)
        .build();
  }
}
