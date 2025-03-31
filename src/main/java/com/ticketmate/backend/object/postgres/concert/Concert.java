package com.ticketmate.backend.object.postgres.concert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Concert extends BasePostgresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID concertId;

    // 공연명
    @Column(nullable = false)
    private String concertName;

    // 공연장
    @ManyToOne(fetch = FetchType.LAZY)
    private ConcertHall concertHall;

    // 공연 카테고리 (콘서트, 뮤지컬...)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcertType concertType;

    // 콘서트 썸네일 이미지 url
    private String concertThumbnailUrl;

    // 좌석 배치도 이미지 url
    private String seatingChartUrl;

    // 예매처
    @Enumerated(EnumType.STRING)
    private TicketReservationSite ticketReservationSite;
}
