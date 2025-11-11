package com.ticketmate.backend.auth.infrastructure.oauth2;

import com.ticketmate.backend.auth.application.validator.AuthValidator;
import com.ticketmate.backend.auth.core.profile.OAuth2Profile;
import com.ticketmate.backend.auth.infrastructure.oauth2.profile.KakaoOAuth2Profile;
import com.ticketmate.backend.auth.infrastructure.oauth2.profile.NaverOAuth2Profile;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.member.application.service.PhoneBlockService;
import com.ticketmate.backend.member.core.constant.AccountStatus;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import com.ticketmate.backend.member.core.vo.Phone;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;
  private final PhoneBlockService phoneBlockService;
  private final AuthValidator authValidator;
  private final ZoneId zoneId;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest request) {
    OAuth2User oAuth2User = super.loadUser(request);
    Map<String, Object> attributes = oAuth2User.getAttributes();
    log.debug("요청된 OAuth2User: {}", attributes);

    String registrationId = request.getClientRegistration().getRegistrationId();
    SocialPlatform socialPlatform = SocialPlatform.fromRegistrationId(registrationId);
    log.debug("요청된 SocialPlatform: {}", socialPlatform);

    OAuth2Profile oAuth2Profile;
    switch (socialPlatform) {
      case NAVER -> oAuth2Profile = new NaverOAuth2Profile(attributes);
      case KAKAO -> oAuth2Profile = new KakaoOAuth2Profile(attributes);
      default -> {
        log.error("네이버 또는 카카오 소셜 로그인 요청만 가능합니다.");
        throw new CustomException(ErrorCode.INVALID_SOCIAL_PLATFORM);
      }
    }
    Phone phone = Phone.of(oAuth2Profile.getPhone());

    // 소셜 로그인 시 전화번호 차단 검증
    if (phone != null) {
      phoneBlockService.ensurePhoneNotBlocked(phone);
    }

    // Member 확인
    Optional<Member> optionalMember = memberRepository
        .findByUsernameAndDeletedFalse(oAuth2Profile.getEmail());
    Member member;
    if (optionalMember.isEmpty()) { // 첫 로그인한 회원인 경우
      member = Member.builder()
          .socialLoginId(oAuth2Profile.getId())
          .username(oAuth2Profile.getEmail())
          .nickname(UUID.randomUUID().toString().substring(0, 12)) // 첫 로그인 시 랜덤 닉네임 부여 (12자리)
          .name(oAuth2Profile.getName())
          .socialPlatform(socialPlatform)
          .birthDay(oAuth2Profile.getBirthDay())
          .birthYear(oAuth2Profile.getBirthYear())
          .phone(phone)
          .profileImgStoredPath(null)
          .gender(oAuth2Profile.getGender())
          .introduction(null)
          .role(Role.ROLE_USER)
          .memberType(MemberType.CLIENT)
          .accountStatus(AccountStatus.ACTIVE)
          .isFirstLogin(true)
          .lastLoginTime(TimeUtil.now())
          .isPhoneNumberVerified(false)
          .isInitialProfileSet(false)
          .followingCount(0L)
          .followerCount(0L)
          .deleted(false)
          .deletedDate(null)
          .build();
    } else { // 첫 로그인이 아닌경우
      member = optionalMember.get();
      // 로그인 가능 여부 검증
      authValidator.assertLoginAllowed(member);

      if (member.getSocialPlatform() != socialPlatform) { // 기존 가입된 소셜 플랫폼과 다른경우
        log.error("기존에 가입한 소셜 로그인 플랫폼이 아닙니다. 현재 요청한 소셜 플랫폼: {}, 기존 회원가입된 소셜 플랫폼: {}",
            socialPlatform, member.getSocialPlatform());
        throw new CustomException(ErrorCode.INVALID_SOCIAL_PLATFORM);
      }
      member.setIsFirstLogin(false);
      member.setLastLoginTime(TimeUtil.now());
    }
    Member savedMember = memberRepository.save(member);

    return new CustomOAuth2User(savedMember, attributes, zoneId);
  }

  // 회원 이메일을 통한 CustomOAuth2User 반환
  @Transactional(readOnly = true)
  public CustomOAuth2User loadUserByUsername(String username) {

    Member member = memberRepository.findByUsernameAndDeletedFalse(username)
        .orElseThrow(() -> {
          log.error("회원을 찾을 수 없습니다. 요청 username: {}", username);
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });

    return new CustomOAuth2User(member, null, zoneId);
  }
}
