package com.ticketmate.backend.domain.member.service;

import static com.ticketmate.backend.global.constant.AuthConstants.ACCESS_TOKEN_KEY;
import static com.ticketmate.backend.global.constant.AuthConstants.REDIS_REFRESH_KEY_PREFIX;
import static com.ticketmate.backend.global.constant.AuthConstants.REFRESH_TOKEN_KEY;

import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.util.auth.AuthUtil;
import com.ticketmate.backend.global.util.auth.CookieUtil;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

  private final JwtUtil jwtUtil;

  /**
   * 쿠키에 저장된 refreshToken을 통해 accessToken, refreshToken을 재발급합니다
   */
  @Transactional
  public void reissue(HttpServletRequest request, HttpServletResponse response) {

    log.debug("accessToken이 만료되어 재발급을 진행합니다.");

    // 쿠키에서 리프레시 토큰 추출 및 검증
    String refreshToken = AuthUtil.extractRefreshTokenFromRequest(request);

    // 사용자 정보 조회
    CustomOAuth2User customOAuth2User = jwtUtil.getCustomOAuth2User(refreshToken);

    // 새로운 토큰 생성
    String newAccessToken = jwtUtil.createAccessToken(customOAuth2User);
    String newRefreshToken = jwtUtil.createRefreshToken(customOAuth2User);

    // 기존 refreshToken 삭제
    jwtUtil.deleteRefreshToken(refreshToken);

    // refreshToken 저장
    // RefreshToken을 Redisd에 저장 (key: RT:memberId)
    jwtUtil.saveRefreshToken(REDIS_REFRESH_KEY_PREFIX + customOAuth2User.getMemberId(), newRefreshToken);

    // 쿠키에 accessToken, refreshToken 추가
    response.addCookie(CookieUtil.createCookie(ACCESS_TOKEN_KEY, newAccessToken, jwtUtil.getAccessExpirationTimeInSeconds()));
    response.addCookie(CookieUtil.createCookie(REFRESH_TOKEN_KEY, newRefreshToken, jwtUtil.getRefreshExpirationTimeInSeconds()));
  }

  /**
   * 요청 된 Member.MemberType 과 MemberType 비교
   *
   * @param member     검증하고자 하는 사용자
   * @param memberType 예상 MemberType
   */
  public void validateMemberType(Member member, MemberType memberType) {
    if (!member.getMemberType().equals(memberType)) {
      log.error("잘못된 MemberType 입니다.. 사용자 MemberType: {}", member.getMemberType());
      throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
    }
  }
}
