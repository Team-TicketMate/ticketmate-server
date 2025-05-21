package com.ticketmate.backend.object.postgres.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Slf4j
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApplicationFormDetail extends BasePostgresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID applicationFormDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationForm applicationForm; // 신청서

    @ManyToOne(fetch = FetchType.LAZY)
    private ConcertDate concertDate; // 공연일자

    @Column(nullable = false)
    @Builder.Default
    private Integer requestCount = 1; // 요청 매수

    @Column(columnDefinition = "TEXT")
    private String requirement; // 요청 사항

    @OneToMany(mappedBy = "applicationFormDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HopeArea> hopeAreaList = new ArrayList<>();

    private static final int HOPE_AREA_MAX_SIZE = 10;

    // 희망구역 설정 메서드
    public void addHopeArea(HopeArea hopeArea) {
        if (hopeAreaList.size() >= HOPE_AREA_MAX_SIZE) {
            throw new CustomException(ErrorCode.HOPE_AREAS_SIZE_EXCEED);
        }
        if (hopeAreaList.stream().anyMatch(area ->
                area.getPriority().equals(hopeArea.getPriority()))) {
            throw new CustomException(ErrorCode.PRIORITY_ALREADY_EXISTS);
        }
        hopeAreaList.add(hopeArea);
        hopeArea.setApplicationFormDetail(this);
    }
}
