package com.ticketmate.backend.auth.infrastructure.bootstrap;

import com.ticketmate.backend.auth.infrastructure.properties.AuthProperties;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시점에 application-*.yml 설정에 따라
 * 관리자 계정을 자동으로 생성/업데이트
 */
@Component
@Slf4j
@RequiredArgsConstructor
@DependsOn("flywayInitializer")
public class AdminBootstrap {

  private final AuthProperties authProperties;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @PostConstruct
  public void initAdmin() {
    authProperties.admins().forEach(admin -> {
      String username = admin.username();
      String rawPassword = admin.password();
      String name = admin.name();
      memberRepository.findByUsername(username)
          .ifPresentOrElse(
              member -> {
                // 패스워드 업데이트
                member.setRole(Role.ROLE_ADMIN);
                member.setPassword(passwordEncoder.encode(rawPassword));
                memberRepository.save(member);
                log.debug("관리자: {} 업데이트 완료", member.getName());
              },
              () -> {
                // 새로운 관리자 계정 생성
                Member newAdmin = Member.builder()
                    .username(username)
                    .role(Role.ROLE_ADMIN)
                    .name(name)
                    .password(passwordEncoder.encode(rawPassword))
                    .totpEnabled(false)
                    .build();
                memberRepository.save(newAdmin);
                log.debug("새로운 관리자: {} 저장 완료", newAdmin.getName());
              }
          );
    });
  }
}
