package com.ticketmate.backend.domain.auth.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class VerifyCodeRequest {
  @NotEmpty(message = "전화번호를 입력해주세요")
  private String phoneNumber;

  @NotEmpty(message = "인증번호 6자리를 입력해주세요")
  @Pattern(regexp = "\\d{6}", message = "인증번호는 숫자 6자리만 입력 가능합니다")
  private String code;
}
