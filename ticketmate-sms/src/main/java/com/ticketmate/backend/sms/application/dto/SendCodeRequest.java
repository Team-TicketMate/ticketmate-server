package com.ticketmate.backend.sms.application.dto;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotEmptyErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.PatternErrorCode;
import jakarta.validation.constraints.NotEmpty;
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
public class SendCodeRequest {

  @NotEmpty
  @NotEmptyErrorCode(ErrorCode.PHONE_NUMBER_EMPTY)
  @Pattern(regexp = "^010[0-9]{8}$")
  @PatternErrorCode(ErrorCode.PHONE_NUMBER_PATTERN_INVALID)
  private String phoneNumber;
}
