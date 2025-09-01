package com.ticketmate.backend.mock.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertAgentAvailability;
import com.ticketmate.backend.member.core.constant.AccountStatus;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.mock.application.dto.request.MockLoginRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhnicknamegenerator.core.SuhRandomKit;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockMemberFactory {

  private final Faker koFaker;
  private final Faker enFaker;
  private final SuhRandomKit suhRandomKit;
  private final Clock clock;

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
  public Member generate(MockLoginRequest request) {
    // 테스트 회원 ROLE 검증
    isValidMemberRoleRequest(request.getRole());

    return baseBuilder(Optional.of(request))
        .role(request.getRole())
        .build();
  }

  /**
   * 공통 필드 설정
   */
  private Member.MemberBuilder<?, ?> baseBuilder(Optional<MockLoginRequest> requestOptional) {
    LocalDate birth = koFaker.timeAndDate().birthday();
    String birthYear = Integer.toString(birth.getYear()); // YYYY
    String birthDay = String.format("%02d%02d", birth.getMonthValue(), birth.getDayOfMonth()); // MMDD

    // username 생성
    String username = requestOptional
        .map(MockLoginRequest::getUsername)
        .filter(s -> !nvl(s, "").isEmpty())
        .orElseGet(() -> enFaker.internet()
            .emailAddress()
            .replaceAll("[^a-zA-Z0-9@.]", ""));

    // 공통 랜덤 필드
    String nickname = suhRandomKit.nicknameWithUuid();
    String name = koFaker.name().name().replaceAll(" ", "");
    SocialPlatform social = requestOptional
        .map(MockLoginRequest::getSocialPlatform)
        .orElseGet(() -> koFaker.options().option(SocialPlatform.class));
    MemberType memberType = requestOptional
        .map(MockLoginRequest::getMemberType)
        .orElseGet(() -> koFaker.options().option(MemberType.class));
    AccountStatus status = requestOptional
        .map(MockLoginRequest::getAccountStatus)
        .orElse(AccountStatus.ACTIVE_ACCOUNT);
    boolean firstLogin = requestOptional
        .map(MockLoginRequest::getIsFirstLogin)
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
        .profileImgStoredPath(koFaker.internet().image() + UUID.randomUUID())
        .gender(koFaker.options().option("male", "female"))
        .memberType(memberType)
        .accountStatus(status)
        .isFirstLogin(firstLogin)
        .lastLoginTime(LocalDateTime.now(clock));
  }

  // 테스트 회원 Role 검증
  private void isValidMemberRoleRequest(Role role) {
    if (!role.equals(Role.ROLE_TEST) && !role.equals(Role.ROLE_TEST_ADMIN)) {
      log.error("Mock 회원은 TEST, TEST_ADMIN 권한만 부여할 수 있습니다. 요청된 ROLE: {}", role);
      throw new CustomException(ErrorCode.INVALID_MEMBER_ROLE_REQUEST);
    }
  }

  // AgentPerformanceSummary 객체 생성 및 랜덤 정수 부여
  public AgentPerformanceSummary generatePerformanceSummary(Member agent) {
    int reviewCount = koFaker.number().numberBetween(0, 100);
    int recentSuccessCount = koFaker.number().numberBetween(0, 50);
    double averageRating = koFaker.number().randomDouble(1, 0, 5);
    long totalScore = koFaker.number().numberBetween(0, 100);

    return AgentPerformanceSummary.builder()
        .agent(agent)
        .reviewCount(reviewCount)
        .averageRating(averageRating)
        .recentSuccessCount(recentSuccessCount)
        .totalScore(totalScore)
        .build();
  }

  // 공연에 대해 대리인의 수락 ON 설정하는 ConcertAgentAvailability 객체 생성
  public ConcertAgentAvailability generateAvailability(Concert concert, Member agent) {
    return ConcertAgentAvailability.builder()
        .concert(concert)
        .agent(agent)
        .accepting(true)
        .introduction(koFaker.lorem().sentence(5, 0))
        .build();
  }
}
