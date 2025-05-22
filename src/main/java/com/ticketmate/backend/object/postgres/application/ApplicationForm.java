package com.ticketmate.backend.object.postgres.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.ApplicationFormStatus;
import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
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
    private TicketOpenDate ticketOpenDate; // 티켓 예매일

    @OneToMany(mappedBy = "applicationForm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationFormDetail> applicationFormDetailList = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRequestCount = 0; // 전체 요청 매수 (모든 공연일자 총 매수)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationFormStatus applicationFormStatus; // 신청서 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketOpenType ticketOpenType; // 선예매, 일반예매 구분

    // 신청서 세부사항 추가 메서드
    public void addApplicationFormDetail(ApplicationFormDetail applicationFormDetail) {
        applicationFormDetailList.add(applicationFormDetail);
        applicationFormDetail.setApplicationForm(this);
    }
}
