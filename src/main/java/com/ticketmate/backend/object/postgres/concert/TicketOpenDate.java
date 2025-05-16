package com.ticketmate.backend.object.postgres.concert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TicketOpenDate extends BasePostgresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID ticketOpenDateId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Concert concert;

    private LocalDateTime openDate; // 티켓 오픈일

    private Integer requestMaxCount; // 최대 예매 매수

    private Boolean isBankTransfer; // 무통장 입금 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketOpenType ticketOpenType; // 선예매, 일반예매 여부
}
