package com.ticketmate.backend.domain.member.service;

import com.ticketmate.backend.domain.member.domain.constant.AccountStatus;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.constant.Role;
import com.ticketmate.backend.domain.member.domain.constant.SocialPlatform;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.dto.response.KakaoResponse;
import com.ticketmate.backend.domain.member.domain.dto.response.NaverResponse;
import com.ticketmate.backend.domain.member.domain.dto.response.OAuth2Response;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.repository.MemberRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest request) {
    OAuth2User oAuth2User = super.loadUser(request);
    log.debug("요청된 OAuth2User: {}", oAuth2User.getAttributes());

    String registrationId = request.getClientRegistration().getRegistrationId();
    SocialPlatform socialPlatform = SocialPlatform.valueOf(registrationId.toUpperCase());
    log.debug("요청된 SocialPlatform: {}", socialPlatform);

    OAuth2Response oAuth2Response;
    if (socialPlatform.equals(SocialPlatform.NAVER)) { // 네이버 소셜 로그인 오쳥 시
      oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
    } else if (socialPlatform.equals(SocialPlatform.KAKAO)) { // 카카오 소셜 로그인 요청 시
      oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
    } else {
      log.error("네이버 또는 카카오 소셜 로그인 요청만 가능합니다.");
      throw new CustomException(ErrorCode.INVALID_SOCIAL_PLATFORM);
    }

    // Member 확인
    Optional<Member> optionalMember = memberRepository.findByUsername(oAuth2Response.getEmail());
    Member member;
    if (optionalMember.isEmpty()) { // 첫 로그인한 회원인 경우
      member = Member.builder()
          .socialLoginId(oAuth2Response.getId())
          .username(oAuth2Response.getEmail())
          .nickname(UUID.randomUUID().toString()) // 첫 로그인 시 랜덤 닉네임 부여
          .name(oAuth2Response.getName())
          .socialPlatform(socialPlatform)
          .birthDay(oAuth2Response.getBirthDay())
          .birthYear(oAuth2Response.getBirthYear())
          .phone(oAuth2Response.getPhone())
          .gender(oAuth2Response.getGender())
          .profileUrl(null)
          .role(Role.ROLE_USER)
          .memberType(MemberType.CLIENT)
          .accountStatus(AccountStatus.ACTIVE_ACCOUNT)
          .isFirstLogin(true)
          .lastLoginTime(LocalDateTime.now())
          .build();
    } else { // 첫 로그인이 아닌경우
      member = optionalMember.get();
      if (!member.getSocialPlatform().equals(socialPlatform)) { // 기존 가입된 소셜 플랫폼과 다른경우
        log.error("기존에 가입한 소셜 로그인 플랫폼이 아닙니다. 현재 요청한 소셜 플랫폼: {}, 기존 회원가입된 소셜 플랫폼: {}",
            socialPlatform, member.getSocialPlatform());
        throw new CustomException(ErrorCode.INVALID_SOCIAL_PLATFORM);
      }
      member.setIsFirstLogin(false);
      member.setLastLoginTime(LocalDateTime.now());
    }
    Member savedMember = memberRepository.save(member);

    return new CustomOAuth2User(savedMember, oAuth2User.getAttributes());
  }

  // 회원 이메일을 통한 CustomOAuth2User 반환
  public CustomOAuth2User loadUserByUsername(String username) {

    Member member = memberRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.error("회원을 찾을 수 없습니다. 요청 username: {}", username);
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });

    return new CustomOAuth2User(member, null);
  }
}
