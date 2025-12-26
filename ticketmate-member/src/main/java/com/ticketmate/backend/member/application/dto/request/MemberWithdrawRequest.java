package com.ticketmate.backend.member.application.dto.request;

import static com.ticketmate.backend.common.core.constant.ValidationConstants.MemberWithdrawal.WITHDRAW_OTHER_REASON_MAX_LENGTH;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import com.ticketmate.backend.member.core.constant.WithdrawalReasonType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberWithdrawRequest {

  @NotNull
  @NotNullErrorCode(ErrorCode.WITHDRAWAL_REASON_TYPE_EMPTY)
  private WithdrawalReasonType withdrawalReasonType;

  @Size(max = WITHDRAW_OTHER_REASON_MAX_LENGTH)
  @SizeErrorCode(ErrorCode.OTHER_REASON_TOO_LONG)
  private String otherReason;
}
