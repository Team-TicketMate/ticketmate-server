package com.ticketmate.backend.service;

import com.ticketmate.backend.object.constants.AccountStatus;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.dto.CustomUserDetails;
import com.ticketmate.backend.object.dto.SignUpRequest;
import com.ticketmate.backend.object.postgres.Member;
import com.ticketmate.backend.repository.postgres.MemberRepository;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 회원가입
     *
     * @param request username, password, nickname, birth, phone, profileUrl
     * @return 없음
     */
    @Transactional
    public void signUp(SignUpRequest request) {

        // 사용자 이메일 검증 (중복 이메일 사용 불가)
        if (memberRepository.existsByUsername(request.getUsername())) {
            log.error("이미 가입된 이메일 주소입니다: {}", request.getUsername());
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        memberRepository.save(Member.builder()
                .username(request.getUsername())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .birth(request.getBirth())
                .phone(request.getPhone())
                .profileUrl(null)
                .role(Role.ROLE_USER)
                .memberType(MemberType.CLIENT)
                .accountStatus(AccountStatus.ACTIVE_ACCOUNT)
                .isFirstLogin(true)
                .lastLoginTime(null)
                .build()
        );
        log.debug("회원가입 성공: username={}", request.getUsername());
    }

    /**
     * 쿠키에 저장된 refreshToken을 통해 accessToken을 재발급합니다.
     */
    @Transactional
    public void reissue(HttpServletRequest request, HttpServletResponse response) {

        log.debug("accessToken이 만료되어 재발급을 진행합니다.");
        String refreshToken = null;

        // 쿠키에서 리프레시 토큰 추출
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        // 리프레시 토큰이 없는 경우
        if (refreshToken == null || refreshToken.isBlank()) {
            log.error("쿠키에서 refreshToken을 찾을 수 없습니다.");
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 해당 refreshToken이 유효한지 검증
        isValidateRefreshToken(refreshToken);

        // 새로운 accessToken 발급
        CustomUserDetails customUserDetails = (CustomUserDetails) jwtUtil
                .getAuthentication(refreshToken).getPrincipal();
        String newAccessToken = jwtUtil.createAccessToken(customUserDetails);

        // 헤더에 accessToken 추가
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        log.debug("accessToken 재발급 성공");
    }

    /**
     * 요청된 리프레시 토큰이 유효한지 확인하고 유효하다면 해당 리프레시 토큰을 반환합니다.
     */
    private void isValidateRefreshToken(String token) {
        if (jwtUtil.isExpired(token)) { // 리프레시 토큰 만료 여부 확인
            log.error("refreshToken이 만료되었습니다.");
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // 토큰이 refresh인지 확인 (발급 시 페이로드에 명시)
        String category = jwtUtil.getCategory(token);
        if (!category.equals("refresh")) {
            log.error("요청된 토큰이 refreshToken이 아닙니다. 요청된 토큰 카테고리: {}", category);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
