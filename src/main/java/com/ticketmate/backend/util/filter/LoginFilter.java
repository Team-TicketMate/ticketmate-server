package com.ticketmate.backend.util.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.CustomUserDetails;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        // 클라이언트 요청에서 username, password 추출
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공 (JWT 발급)
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            Authentication authentication) throws IOException {

        // CustomUserDetails
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.createAccessToken(customUserDetails);
        String refreshToken = jwtUtil.createRefreshToken(customUserDetails);

        log.debug("로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
        log.debug("accessToken = {}", accessToken);
        log.debug("refreshToken = {}", refreshToken);

        // RefreshToken을 Redisd에 저장 (key: RT:memberId)
        redisTemplate.opsForValue().set(
                "RT:" + customUserDetails.getMemberId(),
                refreshToken,
                jwtUtil.getRefreshExpirationTime(),
                TimeUnit.MICROSECONDS
        );

        // JSON 응답
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), ApiResponse.success(tokenMap));
    }

    // 로그인 실패
    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) {
        log.error("로그인 실패");
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
}
