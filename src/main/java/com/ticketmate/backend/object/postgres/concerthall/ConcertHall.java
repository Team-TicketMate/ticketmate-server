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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    private UUID concertHallId;

    // 공연장 이름
    @Column(nullable = false, unique = true)
    private String concertHallName;

    // 수용인원
    private int capacity;

    // 주소
    private String address;

    // city
    @Enumerated(EnumType.STRING)
    private City city;

    // 홈페이지 url
    private String concertHallUrl;
}
