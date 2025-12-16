package com.ticketmate.backend.member.application.dto.request;

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

  @NotNull(message = "withdrawalReasonType이 비어있습니다")
  private WithdrawalReasonType withdrawalReasonType;

  @Size(max = 20, message = "otherReason는 최대 20자 입력 가능합니다")
  private String otherReason;
}
