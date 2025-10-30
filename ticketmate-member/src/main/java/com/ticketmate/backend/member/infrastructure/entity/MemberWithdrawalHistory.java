package com.ticketmate.backend.member.infrastructure.entity;

import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.NICKNAME_MAX_LENGTH;
import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.PHONE_MAX_LENGTH;
import static com.ticketmate.backend.member.infrastructure.constant.BlockConstants.WITHDRAW_REASON_MAX_SIZE;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.core.constant.WithdrawalReasonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
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
public class MemberWithdrawalHistory extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID memberWithdrawalHistoryId;

  @Column(nullable = false)
  private UUID memberId;

  @Column(nullable = false, length = PHONE_MAX_LENGTH)
  private String phone;

  @Column(nullable = false, length = NICKNAME_MAX_LENGTH)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WithdrawalReasonType withdrawalReasonType;

  @Column(length = WITHDRAW_REASON_MAX_SIZE)
  private String otherReason;

  public static MemberWithdrawalHistory create(UUID memberId, String phone, String nickname, WithdrawalReasonType withdrawalReasonType, String otherReason) {
    return MemberWithdrawalHistory.builder()
        .memberId(memberId)
        .phone(phone)
        .nickname(nickname)
        .withdrawalReasonType(withdrawalReasonType)
        .otherReason(otherReason)
        .build();
  }
}
