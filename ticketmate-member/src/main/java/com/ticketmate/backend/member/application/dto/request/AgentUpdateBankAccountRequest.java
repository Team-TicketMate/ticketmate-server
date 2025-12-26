package com.ticketmate.backend.member.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.PatternErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import com.ticketmate.backend.member.core.constant.BankCode;
import jakarta.validation.constraints.Pattern;
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
public class AgentUpdateBankAccountRequest {
  private BankCode bankCode;  // 은행 정보

  @Size(max = 20)
  @SizeErrorCode(ErrorCode.ACCOUNT_HOLDER_TOO_LONG)
  @Pattern(regexp = ".*\\S.*")
  @PatternErrorCode(ErrorCode.ACCOUNT_HOLDER_WHITESPACE_ONLY)
  private String accountHolder;  // 예금주

  @Pattern(regexp = "^[0-9]{11,16}$")
  @PatternErrorCode(ErrorCode.ACCOUNT_NUMBER_PATTERN_INVALID)
  private String accountNumber;  // "-" 제거된 계좌번호
}
