package com.ticketmate.backend.object.dto.test.request;

import com.ticketmate.backend.object.constants.AccountStatus;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.constants.SocialPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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

  @Email
  @Schema(defaultValue = "example@naver.com")
  private String username;

  @Schema(defaultValue = "ROLE_TEST")
  private Role role;

  @Schema(defaultValue = "NAVER")
  private SocialPlatform socialPlatform;

  @Schema(defaultValue = "CLIENT")
  private MemberType memberType;

  @Schema(defaultValue = "ACTIVE_ACCOUNT")
  private AccountStatus accountStatus;

  @Schema(defaultValue = "false")
  private Boolean isFirstLogin;
}
