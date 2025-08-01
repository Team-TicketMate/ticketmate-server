package com.ticketmate.backend.mock.application.dto.request;

import com.ticketmate.backend.member.core.constant.AccountStatus;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
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
  private String username;

  private Role role;

  private SocialPlatform socialPlatform;

  private MemberType memberType;

  private AccountStatus accountStatus;

  private Boolean isFirstLogin;
}
