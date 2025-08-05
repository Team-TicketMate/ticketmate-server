package com.ticketmate.backend.applicationform.infrastructure.entity;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Slf4j
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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationFormStatus applicationFormStatus; // 신청서 상태

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TicketOpenType ticketOpenType; // 선예매, 일반예매 구분

  // 신청서 세부사항 추가 메서드
  public void addApplicationFormDetail(ApplicationFormDetail applicationFormDetail) {
    if (applicationFormDetail == null) {
      log.error("신청서 세부사항 데이터가 null 입니다.");
      throw new CustomException(ErrorCode.APPLICATION_FORM_DETAIL_NOT_FOUND);
    }
    if (applicationFormDetailList.contains(applicationFormDetail)) {
      log.error("이미 존재하는 신청서 세부사항입니다.");
      throw new CustomException(ErrorCode.DUPLICATE_APPLICATION_FORM_DETAIL);
    }
    applicationFormDetailList.add(applicationFormDetail);
    applicationFormDetail.setApplicationForm(this);
  }
}
