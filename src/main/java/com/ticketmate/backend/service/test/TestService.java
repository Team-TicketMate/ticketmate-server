package com.ticketmate.backend.service.test;

import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import com.ticketmate.backend.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {

    private final MemberRepository memberRepository;
    private final Faker koFaker = new Faker(new Locale("ko", "KR"));
    private final Faker enFaker = new Faker(new Locale("en"));
    private final JwtUtil jwtUtil;

    /**
     * 개발자용 테스트 로그인 로직
     * DB에 테스트 유저를 만든 후, 해당 사용자의 엑세스 토큰을 발급합니다.
     *
     * @param request socialPlatform 네이버/카카오 소셜 로그인 플랫폼
     *                memberType 의로인/대리인
     *                accountStatus 활성화/삭제
     *                isFirstLogin 첫 로그인 여부
     */
    @Transactional
    public String testSocialLogin(LoginRequest request) {
        LocalDate birth = koFaker.timeAndDate().birthday();
        log.debug("Faker 생성 birth: {}", birth);
        String birthYear = Integer.toString(birth.getYear()); // YYYY
        String birthDay = String.format("%02d%02d", birth.getMonthValue(), birth.getDayOfMonth()); // MMDD

        log.debug("테스트 계정 로그인을 집행합니다. 요청 소셜 플랫폼: {}", request.getSocialPlatform());
        Member testMember = Member.builder()
                .socialLoginId(UUID.randomUUID().toString())
                .username(enFaker.internet().emailAddress().replaceAll("[^a-zA-Z0-9@\\s]", ""))
                .nickname(enFaker.lorem().word())
                .name(koFaker.name().name().replaceAll(" ",""))
                .socialPlatform(request.getSocialPlatform())
                .birthDay(birthYear)
                .birthYear(birthDay)
                .phone(koFaker.phoneNumber().cellPhone())
                .profileUrl(koFaker.internet().image())
                .gender(koFaker.options().option("male", "female"))
                .role(Role.ROLE_TEST)
                .memberType(request.getMemberType())
                .accountStatus(request.getAccountStatus())
                .isFirstLogin(request.getIsFirstLogin())
                .lastLoginTime(LocalDateTime.now())
                .build();
        memberRepository.save(testMember);

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(testMember, null);
        String accessToken = jwtUtil.createAccessToken(customOAuth2User);

        log.debug("테스트 로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
        log.debug("테스트 accessToken = {}", accessToken);

        return accessToken;
    }

    /**
     * 데이터베이스에 저장되어있는 테스트 유저를 모두 삭제합니다
     */
    @Transactional
    public void deleteTestMember() {
        log.debug("데이터베이스에 저장된 테스트 유저를 모두 삭제합니다.");
        memberRepository.deleteAllByRole(Role.ROLE_TEST);
    }
}
