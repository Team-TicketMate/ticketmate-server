package com.ticketmate.backend.object;

import com.ticketmate.backend.object.constants.AccountStatus;
import com.ticketmate.backend.object.postgres.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final Member member;
  private Map<String, Object> attributes;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  public CustomUserDetails(Member member, Map<String, Object> attributes) {
    this.member = member;
    this.attributes = attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()));
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public String getUsername() {
    return member.getMemberId().toString(); // memberId UUID를 문자열로 변환 후 반환
  }

  @Override
  public boolean isAccountNonExpired() {
    // AccountStatus가 DELETE_ACCOUNT 인 경우, 계정이 만료된 것으로 간주
    return member.getAccountStatus() != AccountStatus.DELETE_ACCOUNT;
  }

  @Override
  public boolean isAccountNonLocked() {
    // AccountStatus가 DELETE_ACCOUNT 인 경우, 계정이 잠긴 것으로 간주
    return member.getAccountStatus() != AccountStatus.DELETE_ACCOUNT;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // 인정 정보 항상 유효
  }

  @Override
  public boolean isEnabled() {
    // AccountStatus가 ACTIVE_ACCOUNT 인 경우, 계정이 활성화
    return member.getAccountStatus() != AccountStatus.DELETE_ACCOUNT;
  }
}
