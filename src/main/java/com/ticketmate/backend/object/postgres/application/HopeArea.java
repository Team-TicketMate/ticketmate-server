package com.ticketmate.backend.object.postgres.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class HopeArea extends BasePostgresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID hopeAreaId;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationForm applicationForm; // 신청서

    @Column(nullable = false)
    private Integer priority; // 순위 (1~10)

    @Column(nullable = false)
    private String location; // 위치 (예: A구역, B구역)

    @Column(nullable = false)
    private Long price; // 가격
}
