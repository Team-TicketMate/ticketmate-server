package com.ticketmate.backend.member.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.PatternErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import com.ticketmate.backend.member.core.constant.BankCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AgentSaveBankAccountRequest {
  @NotNull
  @NotNullErrorCode(ErrorCode.BANK_CODE_EMPTY)
  private BankCode bankCode;  // 은행 코드

  @NotBlank
  @NotBlankErrorCode(ErrorCode.ACCOUNT_HOLDER_EMPTY)
  @Size(max = 20)
  @SizeErrorCode(ErrorCode.ACCOUNT_HOLDER_TOO_LONG)
  @Pattern(regexp = ".*\\S.*")
  @PatternErrorCode(ErrorCode.ACCOUNT_HOLDER_WHITESPACE_ONLY)
  private String accountHolder;  // 예금주

  @NotBlank
  @NotBlankErrorCode(ErrorCode.ACCOUNT_NUMBER_EMPTY)
  @Pattern(regexp = "^[0-9]{11,16}$")
  @PatternErrorCode(ErrorCode.ACCOUNT_NUMBER_PATTERN_INVALID)
  private String accountNumber;  // "-" 제거된 계좌번호

  @NotNull
  @NotNullErrorCode(ErrorCode.PRIMARY_ACCOUNT_EMPTY)
  private boolean primaryAccount;  // 대표계좌 유/무 (등록하면서 바로 설정)
}
