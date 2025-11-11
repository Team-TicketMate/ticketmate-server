package com.ticketmate.backend.mock.application.dto.request;

import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import jakarta.validation.constraints.Email;
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
public class MockLoginRequest {

  @Email
  private String username;

  private Role role;

  private SocialPlatform socialPlatform;

  private MemberType memberType;

  private Boolean isFirstLogin;

  private boolean isPhoneNumberVerified;

  private boolean isInitialProfileSet;
}
