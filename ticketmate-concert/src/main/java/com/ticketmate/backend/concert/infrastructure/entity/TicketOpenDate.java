package com.ticketmate.backend.concert.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
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
public class TicketOpenDate extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID ticketOpenDateId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Concert concert;

  @Column(columnDefinition = "TIMESTAMP(0)")
  private LocalDateTime openDate; // 티켓 오픈일

  private Integer requestMaxCount; // 최대 예매 매수

  private Boolean isBankTransfer; // 무통장 입금 여부

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TicketOpenType ticketOpenType; // 선예매, 일반예매 여부

  public static TicketOpenDate of(Concert concert, LocalDateTime openDate, Integer requestMaxCount, Boolean isBankTransfer, TicketOpenType ticketOpenType) {
    return TicketOpenDate.builder()
        .concert(concert)
        .openDate(openDate)
        .requestMaxCount(requestMaxCount)
        .isBankTransfer(isBankTransfer)
        .ticketOpenType(ticketOpenType)
        .build();
  }
}
