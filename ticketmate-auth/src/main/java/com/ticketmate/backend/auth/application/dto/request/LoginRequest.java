package com.ticketmate.backend.auth.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import jakarta.validation.constraints.NotBlank;
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
public class LoginRequest {

  @NotBlank
  @NotBlankErrorCode(ErrorCode.USERNAME_EMPTY)
  private String username;

  @NotBlank
  @NotBlankErrorCode(ErrorCode.PASSWORD_EMPTY)
  private String password;
}
