package com.ticketmate.backend.sms.application.dto;

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

  @NotEmpty(message = "전화번호를 입력해주세요")
  @Pattern(regexp = "^010[0-9]{8}$", message = "전화번호는 010으로 시작하는 11자리 문자열만 입력가능합니다 (예: 01012345678)")
  private String phoneNumber;
}
