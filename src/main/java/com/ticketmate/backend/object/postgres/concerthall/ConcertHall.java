package com.ticketmate.backend.object.postgres.concerthall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.City;
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
public class ConcertHall extends BasePostgresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID concertHallId;

    @Column(nullable = false)
    private String concertHallName; // 공연장 명

    private String address; // 주소

    @Enumerated(EnumType.STRING)
    private City city; // 지역

    private String webSiteUrl; // 사이트 URL
}
