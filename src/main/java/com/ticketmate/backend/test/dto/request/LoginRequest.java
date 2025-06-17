package com.ticketmate.backend.test.dto.request;

import com.ticketmate.backend.domain.member.domain.constant.AccountStatus;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.constant.Role;
import com.ticketmate.backend.domain.member.domain.constant.SocialPlatform;
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
