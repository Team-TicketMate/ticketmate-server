package com.ticketmate.backend.service.member.oauth2;

import com.ticketmate.backend.object.constants.AccountStatus;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.constants.SocialPlatform;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.auth.response.KakaoResponse;
import com.ticketmate.backend.object.dto.auth.response.NaverResponse;
import com.ticketmate.backend.object.dto.auth.response.OAuth2Response;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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
        Member member = memberRepository.findByUsername(oAuth2Response.getEmail());
        if (member == null) { // 첫 로그인한 회원인 경우
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

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            log.error("회원을 찾을 수 없습니다. 요청 username: {}", username);
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return new CustomOAuth2User(member, null);
    }
}
