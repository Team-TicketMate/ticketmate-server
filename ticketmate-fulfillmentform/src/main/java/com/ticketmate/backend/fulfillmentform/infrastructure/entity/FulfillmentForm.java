package com.ticketmate.backend.fulfillmentform.infrastructure.entity;

import static com.ticketmate.backend.common.core.constant.ValidationConstants.FulfillmentForm.FULFILLMENT_IMG_MAX_COUNT;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.fulfillmentform.core.constant.fulfillmentform.FulfillmentFormStatus;
import com.ticketmate.backend.member.infrastructure.entity.AgentBankAccount;
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
public class FulfillmentForm extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID fulfillmentFormId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member client; // 의뢰인

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member agent; // 대리인

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Concert concert;  // 콘서트

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private ApplicationForm applicationForm;  // 신청서

  @Column(nullable = false)
  private String chatRoomId;  // 채팅방 ID

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private AgentBankAccount agentBankAccount;  // 대리인 계좌 정보

  @Builder.Default
  @OneToMany(mappedBy = "fulfillmentForm", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FulfillmentFormImg> successTicketingStoredPathList = new ArrayList<>();  // 의뢰인에게 보낼 성공 사진값 (선택)

  @Column(length = 100, nullable = false)
  private String particularMemo;  // 상세 설명

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FulfillmentFormStatus fulfillmentFormStatus;

  public static FulfillmentForm create(Member client, Member agent, Concert concert, ApplicationForm applicationForm,
    String chatRoomId, AgentBankAccount agentBankAccount, String particularMemo, FulfillmentFormStatus fulfillmentFormStatus) {
    return FulfillmentForm.builder()
      .client(client)
      .agent(agent)
      .concert(concert)
      .applicationForm(applicationForm)
      .chatRoomId(chatRoomId)
      .agentBankAccount(agentBankAccount)
      .particularMemo(particularMemo)
      .fulfillmentFormStatus(fulfillmentFormStatus)
      .build();
  }

  public void addFulfillmentFormImg(FulfillmentFormImg img) {
    if (this.getSuccessTicketingStoredPathList().size() >= FULFILLMENT_IMG_MAX_COUNT) {
      throw new CustomException(ErrorCode.INVALID_FULFILLMENT_FORM_IMG_COUNT);
    }

    this.successTicketingStoredPathList.add(img);
    img.setFulfillmentForm(this);
  }

  public void removeFulfillmentFormImg(FulfillmentFormImg fulfillmentFormImg) {
    this.successTicketingStoredPathList.remove(fulfillmentFormImg);
  }
}
