package com.ticketmate.backend.test.service;

import static com.ticketmate.backend.global.util.common.CommonUtil.nvl;

import com.ticketmate.backend.domain.member.domain.constant.AccountStatus;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.constant.Role;
import com.ticketmate.backend.domain.member.domain.constant.SocialPlatform;
import com.ticketmate.backend.test.dto.request.LoginRequest;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockMemberFactory {

  private final Faker koFaker;
  private final Faker enFaker;

  /**
   * 개발자용 회원 Mock 데이터 생성
   */
  public Member generate() {
    return baseBuilder(Optional.empty())
        .role(Role.ROLE_TEST)
        .build();
  }

  /**
   * 개발자용 회원 Mock 데이터 생성
   *
   * @param request username 이메일 (선택)
   *                role 권한
   *                socialPlatform 네이버/카카오 소셜 로그인 플랫폼
   *                memberType 의로인/대리인
   *                accountStatus 활성화/삭제
   *                isFirstLogin 첫 로그인 여부
   */
  public Member generate(LoginRequest request) {
    // 테스트 회원 ROLE 검증
    isValidMemberRoleRequest(request.getRole());

    return baseBuilder(Optional.of(request))
        .role(request.getRole())
        .build();
  }

  /**
   * 공통 필드 설정
   */
  private Member.MemberBuilder<?, ?> baseBuilder(Optional<LoginRequest> requestOptional) {
    LocalDate birth = koFaker.timeAndDate().birthday();
    String birthYear = Integer.toString(birth.getYear()); // YYYY
    String birthDay = String.format("%02d%02d", birth.getMonthValue(), birth.getDayOfMonth()); // MMDD

    // username 생성
    String username = requestOptional
        .map(LoginRequest::getUsername)
        .filter(s -> !nvl(s, "").isEmpty())
        .orElseGet(() -> enFaker.internet()
            .emailAddress()
            .replaceAll("[^a-zA-Z0-9@.]", ""));

    // 공통 랜덤 필드
    String nickname = UUID.randomUUID().toString();
    String name = koFaker.name().name().replaceAll(" ", "");
    SocialPlatform social = requestOptional
        .map(LoginRequest::getSocialPlatform)
        .orElseGet(() -> koFaker.options().option(SocialPlatform.class));
    MemberType memberType = requestOptional
        .map(LoginRequest::getMemberType)
        .orElseGet(() -> koFaker.options().option(MemberType.class));
    AccountStatus status = requestOptional
        .map(LoginRequest::getAccountStatus)
        .orElse(AccountStatus.ACTIVE_ACCOUNT);
    boolean firstLogin = requestOptional
        .map(LoginRequest::getIsFirstLogin)
        .orElseGet(() -> koFaker.random().nextBoolean());

    return Member.builder()
        .socialLoginId(UUID.randomUUID().toString())
        .username(username + koFaker.random().nextInt(1000))
        .nickname(nickname)
        .name(name)
        .socialPlatform(social)
        .birthYear(birthYear)
        .birthDay(birthDay)
        .phone(koFaker.phoneNumber().cellPhone())
        .profileUrl(koFaker.internet().image())
        .gender(koFaker.options().option("male", "female"))
        .memberType(memberType)
        .accountStatus(status)
        .isFirstLogin(firstLogin)
        .lastLoginTime(LocalDateTime.now());
  }

  // 테스트 회원 Role 검증
  private void isValidMemberRoleRequest(Role role) {
    if (!role.equals(Role.ROLE_TEST) && !role.equals(Role.ROLE_TEST_ADMIN)) {
      log.error("Mock 회원은 TEST, TEST_ADMIN 권한만 부여할 수 있습니다. 요청된 ROLE: {}", role);
      throw new CustomException(ErrorCode.INVALID_MEMBER_ROLE_REQUEST);
    }
  }
}
