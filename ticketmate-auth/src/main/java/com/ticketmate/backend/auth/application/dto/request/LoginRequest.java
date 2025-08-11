package com.ticketmate.backend.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class LoginRequest {

  @NotBlank(message = "로그인 아이디를 입력하세요")
  private String username;

  @NotBlank(message = "로그인 비밀번호를 입력하세요")
  private String password;
}
