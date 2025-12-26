package com.ticketmate.backend.member.infrastructure.entity;

import static com.ticketmate.backend.common.core.constant.ValidationConstants.Member.NICKNAME_MAX_LENGTH;
import static com.ticketmate.backend.common.core.constant.ValidationConstants.Member.PHONE_MAX_LENGTH;
import static com.ticketmate.backend.common.core.constant.ValidationConstants.MemberWithdrawal.WITHDRAW_OTHER_REASON_MAX_LENGTH;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.core.constant.WithdrawalReasonType;
import com.ticketmate.backend.member.core.vo.Phone;
import com.ticketmate.backend.member.infrastructure.converter.PhoneJpaConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
  @Convert(converter = PhoneJpaConverter.class)
  private Phone phone;

  @Column(nullable = false, length = NICKNAME_MAX_LENGTH)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WithdrawalReasonType withdrawalReasonType;

  @Column(length = WITHDRAW_OTHER_REASON_MAX_LENGTH)
  private String otherReason;

  public static MemberWithdrawalHistory create(UUID memberId, Phone phone, String nickname, WithdrawalReasonType withdrawalReasonType, String otherReason) {
    return MemberWithdrawalHistory.builder()
      .memberId(memberId)
      .phone(phone)
      .nickname(nickname)
      .withdrawalReasonType(withdrawalReasonType)
      .otherReason(otherReason)
      .build();
  }
}
