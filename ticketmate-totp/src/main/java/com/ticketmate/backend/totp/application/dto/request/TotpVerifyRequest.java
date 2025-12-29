package com.ticketmate.backend.totp.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.PatternErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class TotpVerifyRequest {

  @NotBlank
  @NotBlankErrorCode(ErrorCode.TOTP_CODE_EMPTY)
  @Pattern(regexp = "^[0-9]{6}$")
  @PatternErrorCode(ErrorCode.TOTP_CODE_PATTERN_INVALID)
  private String code;
}
