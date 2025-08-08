package com.ticketmate.backend.totp.application.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotpVerifyRequest {

  @Pattern(regexp = "^\\d{6}$", message = "TOTP 코드는 6자리 숫자여야 합니다")
  private String code;
}
