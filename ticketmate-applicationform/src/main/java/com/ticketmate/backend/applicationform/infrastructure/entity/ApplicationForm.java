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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationForm extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, unique = true)
  private UUID applicationFormId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member client; // 의뢰인

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member agent; // 대리인

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Concert concert; // 공연

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private TicketOpenDate ticketOpenDate; // 티켓 예매일

  @OneToMany(mappedBy = "applicationForm", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ApplicationFormDetail> applicationFormDetailList = new ArrayList<>();

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationFormStatus applicationFormStatus = ApplicationFormStatus.PENDING; // 신청서 상태

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TicketOpenType ticketOpenType; // 선예매, 일반예매 구분

  public static ApplicationForm create(Member client, Member agent, Concert concert, TicketOpenDate ticketOpenDate, TicketOpenType ticketOpenType) {
    return ApplicationForm.builder()
        .client(client)
        .agent(agent)
        .concert(concert)
        .ticketOpenDate(ticketOpenDate)
        .applicationFormDetailList(new ArrayList<>())
        .applicationFormStatus(ApplicationFormStatus.PENDING) // 신청서는 기본 "대기" 상태
        .ticketOpenType(ticketOpenType)
        .build();
  }

  // 신청서 세부사항 추가 메서드
  public void addApplicationFormDetail(ApplicationFormDetail applicationFormDetail) {
    if (applicationFormDetail == null) {
      throw new CustomException(ErrorCode.APPLICATION_FORM_DETAIL_NOT_FOUND);
    }
    if (applicationFormDetailList.contains(applicationFormDetail)) {
      throw new CustomException(ErrorCode.DUPLICATE_APPLICATION_FORM_DETAIL);
    }
    applicationFormDetailList.add(applicationFormDetail);
    applicationFormDetail.setApplicationForm(this);
  }

  public void unAssignAgent() {
    this.agent = null;
  }
}
