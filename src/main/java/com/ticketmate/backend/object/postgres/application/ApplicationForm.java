package com.ticketmate.backend.object.postgres.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.ApplicationFormStatus;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.concert.Concert;
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
public class ApplicationForm extends BasePostgresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID applicationFormId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member client; // 의뢰인

    @ManyToOne(fetch = FetchType.LAZY)
    private Member agent; // 대리인

    @ManyToOne(fetch = FetchType.LAZY)
    private Concert concert; // 공연

    @ManyToOne(fetch = FetchType.LAZY)
    private ConcertDate concertDate; // 공연일자

    @Column(nullable = false)
    @Builder.Default
    private Integer requestCount = 1; // 매수

    @OneToMany(mappedBy = "applicationForm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HopeArea> hopeAreaList = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String requestDetails; // 요청사항

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationFormStatus applicationFormStatus; // 신청서 상태


    private static final int HOPE_AREAS_MAX_SIZE = 10;

    // 희망구역 설정
    public void addHopeArea(HopeArea hopeArea) {
        if (hopeAreaList.size() >= HOPE_AREAS_MAX_SIZE) {
            log.error("희망구역은 최대 {}개까지만 설정 가능합니다. 현재 희망구역 개수: {}",
                    HOPE_AREAS_MAX_SIZE, hopeAreaList.size());
            throw new CustomException(ErrorCode.HOPE_AREAS_SIZE_EXCEED);
        }
        if (hopeAreaList.stream().anyMatch(area ->
                area.getPriority().equals(hopeArea.getPriority()))) {
            log.error("해당 순위는 이미 설정되어 있습니다. 요청된 순위: {}", hopeArea.getPriority());
            throw new CustomException(ErrorCode.PRIORITY_ALREADY_EXISTS);
        }
        hopeAreaList.add(hopeArea);
        hopeArea.setApplicationForm(this);
    }
}
