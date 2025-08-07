package com.ticketmate.backend.auth.infrastructure.bootstrap;

import com.ticketmate.backend.auth.infrastructure.properties.AuthProperties;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시점에 application-*.yml 설정에 따라
 * 관리자 계정을 자동으로 생성/업데이트
 */
@Component
@RequiredArgsConstructor
public class AdminBootstrap {

  private final AuthProperties authProperties;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @PostConstruct
  public void initAdmin() {
    String adminUsername = authProperties.username();
    String rawPassword = authProperties.password();
    String totpSecret = authProperties.totpSecret();

    memberRepository.findByUsername(adminUsername)
        .ifPresentOrElse(
            member -> {
              // 패스워드 및 TOTP 업데이트
              member.setPassword(passwordEncoder.encode(rawPassword));
              member.setTotpEnabled(true);
              member.setTotpSecret(totpSecret);
              memberRepository.save(member);
            },
            () -> {
              // 새로운 관리자 계정 생성
              Member admin = Member.builder()
                  .username(adminUsername)
                  .password(passwordEncoder.encode(rawPassword))
                  .role(Role.ROLE_ADMIN)
                  .name(authProperties.name())
                  .totpEnabled(true)
                  .totpSecret(totpSecret)
                  .build();
              memberRepository.save(admin);
            }
        );
  }
}
