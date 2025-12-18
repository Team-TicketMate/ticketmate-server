package com.ticketmate.backend.totp.application.dto.request;

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

  @NotBlank(message = "TOTP code가 비어있습니다")
  @Pattern(regexp = "^[0-9]{6}$", message = "TOTP 코드는 6자리 숫자여야 합니다")
  private String code;
}
