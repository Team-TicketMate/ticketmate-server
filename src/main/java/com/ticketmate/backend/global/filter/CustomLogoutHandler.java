package com.ticketmate.backend.global.filter;

import static com.ticketmate.backend.global.constant.AuthConstants.REDIS_REFRESH_KEY_PREFIX;
import static com.ticketmate.backend.global.constant.AuthConstants.REFRESH_TOKEN_KEY;

import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.repository.MemberRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;
  private final MemberRepository memberRepository;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // 1. 요청 헤더에서 엑세스 토큰 추출
    String authHeader = request.getHeader("Authorization");
    String accessToken = null;
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      accessToken = authHeader.substring(7);
    }

    // 2. 쿠키에서 리프레시 토큰 추출 및 삭제
    String refreshToken = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (REFRESH_TOKEN_KEY.equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          // 쿠키 삭제
          cookie.setMaxAge(0);
          cookie.setPath("/");
          response.addCookie(cookie);
          log.debug("리프레시 토큰 쿠키 삭제 완료");
          break;
        }
      }
    }

    // 3. Redis에서 리프레시 토큰 삭제
    if (authentication != null && refreshToken != null) {
      String username = jwtUtil.getUsername(refreshToken);
      Member member = memberRepository.findByUsername(username)
          .orElseThrow(() -> {
            log.error("리프레시 토큰에 등록된 사용자를 찾을 수 없습니다.");
            return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
          });

      // 리프레시 토큰 삭제
      redisTemplate.delete(REDIS_REFRESH_KEY_PREFIX + member.getMemberId());
      log.debug("Redis에서 리프레시 토큰 삭제 완료: RT:{}", member.getMemberId());
    }

    // 4. 엑세스 토큰 블랙리스트 처리
    if (accessToken != null && jwtUtil.validateToken(accessToken)) {
      jwtUtil.blacklistAccessToken(accessToken);
    }
  }
}

