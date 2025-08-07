package com.ticketmate.backend.auth.infrastructure.admin;

import com.ticketmate.backend.auth.core.principal.UserPrincipal;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomAdminUser implements UserDetails, UserPrincipal {

  private final Member member;

  @Override
  public String getMemberId() {
    return member.getMemberId().toString();
  }

  @Override
  public List<String> getRoles() {
    return List.of(member.getRole().name());
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
    return member.getUsername();
  }
}
