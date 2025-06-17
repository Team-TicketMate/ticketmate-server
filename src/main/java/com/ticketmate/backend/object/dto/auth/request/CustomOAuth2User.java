package com.ticketmate.backend.object.dto.auth.request;

import com.ticketmate.backend.object.constants.AccountStatus;
import com.ticketmate.backend.object.constants.SocialPlatform;
import com.ticketmate.backend.object.postgres.Member.Member;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User, Principal {

  private final Member member;
  private final Map<String, Object> attributes;

  // JWT 액세스 토큰의 만료 시각 (채팅에서 사용할 기능입니다.)
  private LocalDateTime expiresAt = null;

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()));
  }

  @Override
  public String getName() {
    return member.getName(); // 이름
  }

  public String getUsername() {
    return member.getUsername(); // 이메일
  }

  public SocialPlatform getSocialPlatform() {
    return member.getSocialPlatform(); // 소셜 플랫폼
  }

  public boolean isAccountNonExpired() {
    // AccountStatus가 DELETE_ACCOUNT 인 경우, 계정이 만료된 것으로 간주
    return member.getAccountStatus() != AccountStatus.DELETE_ACCOUNT;
  }

  public boolean isAccountNonLocked() {
    // AccountStatus가 DELETE_ACCOUNT 인 경우, 계정이 잠긴 것으로 간주
    return member.getAccountStatus() != AccountStatus.DELETE_ACCOUNT;
  }

  public boolean isCredentialsNonExpired() {
    return true; // 인증 정보 항상 유효
  }

  public boolean isEnabled() {
    // AccountStatus가 ACTIVE_ACCOUNT 인 경우, 계정이 활성화
    return member.getAccountStatus() != AccountStatus.DELETE_ACCOUNT;
  }

  public String getMemberId() {
    return member.getMemberId().toString(); // 회원의 memberId (UUID)를 string 으로 반환
  }

  public void confirmExpire(long expiresAtMillis) {
    this.expiresAt = LocalDateTime.now().plus(expiresAtMillis, ChronoUnit.MILLIS);
  }
}
