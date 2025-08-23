package com.ticketmate.backend.auth.infrastructure.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.infrastructure.constant.AuthConstants;
import com.ticketmate.backend.auth.infrastructure.constant.SecurityUrls;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2UserService;
import com.ticketmate.backend.auth.infrastructure.util.AuthUtil;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.ErrorResponse;
import com.ticketmate.backend.common.core.util.CommonUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 토큰 기반 인증 필터
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final TokenProvider tokenProvider;
  private final CustomOAuth2UserService customOAuth2UserService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();
    log.debug("요청된 URI: {}", uri);
    ApiRequestType apiRequestType = determineApiRequestType(uri);

    // 화이트리스트 체크 : 화이트리스트 경로면 필터링 건너뜀
    if (isWhitelistedPath(uri)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = AuthUtil.extractAccessTokenFromRequest(request);

      // 토큰 검증: 토큰이 유효하면 인증 설정
      if (token != null && tokenProvider.isValidToken(token)) {
        handleValidToken(request, response, filterChain, token, apiRequestType);
        return;
      }

      // 선택적 인증 경로이고 토큰이 없는 경우 통과
      if (isOptionalAuthPath(uri) && token == null) {
        filterChain.doFilter(request, response);
        return;
      }

      handleInvalidToken(response, token);
    } catch (ExpiredJwtException e) {
      log.error("토큰 만료: {}", e.getMessage());
      sendErrorResponse(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
    }
  }

  /**
   * 에러 응답을 JSON 형태로 클라이언트에 전송
   *
   * @param response  HttpServletResponse 객체
   * @param errorCode 발생한 에러코드
   */
  private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(errorCode.getStatus().value());
    response.setCharacterEncoding("UTF-8");

    ErrorResponse errorResponse = new ErrorResponse(errorCode, errorCode.getMessage());

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getWriter(), errorResponse);
  }

  /**
   * URI에 따른 요청 타입을 결정합니다
   */
  private ApiRequestType determineApiRequestType(String uri) {
    if (uri.startsWith(AuthConstants.API_RESPONSE_PREFIX)) {
      return ApiRequestType.API;
    } else if (uri.startsWith(AuthConstants.ADMIN_RESPONSE_PREFIX)) {
      return ApiRequestType.ADMIN;
    }
    return ApiRequestType.OTHER;
  }

  /**
   * 화이트리스트 경로 확인 (인증x)
   *
   * @param uri 요청된 URI
   * @return 화이트리스트 여부
   */
  private boolean isWhitelistedPath(String uri) {
    return SecurityUrls.AUTH_WHITELIST.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, uri));
  }

  /**
   * 선택적 인증 경로 확인
   *
   * @param uri 요청된 URI
   * @return 선택적 인증 경로 여부
   */
  private boolean isOptionalAuthPath(String uri) {
    return SecurityUrls.OPTIONAL_AUTH_PATHS.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, uri));
  }

  /**
   * 관리자 권한 확인
   *
   * @param token JWT
   * @return 관리자 권한 여부
   */
  private boolean hasAdminRole(String token) {
    return tokenProvider.getRole(token).equals("ROLE_ADMIN");
  }

  /**
   * 테스트 계정 확인
   * TODO: 추후 삭제
   */
  private boolean hasTestAdminRole(String token) {
    return tokenProvider.getRole(token).equals("ROLE_TEST_ADMIN");
  }

  /**
   * 유효한 토큰 처리
   */
  private void handleValidToken(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain,
      String token,
      ApiRequestType apiRequestType
  ) throws IOException, ServletException {
    String username = tokenProvider.getUsername(token);

    CustomOAuth2User customOAuth2User = customOAuth2UserService.loadUserByUsername(username);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities())
    );

    // 관리자 페이지 접근 권한 체크: 관리자 권한 없으면 로그인 페이지로 리다이렉트 TODO: 추후 테스트계정 권한 삭제
    if (apiRequestType.equals(ApiRequestType.ADMIN) && !hasAdminRole(token) && !hasTestAdminRole(token)) {
      log.error("관리자 권한이 없습니다.");
      sendErrorResponse(response, ErrorCode.ACCESS_DENIED);
      return;
    }

    // 인증 성공
    filterChain.doFilter(request, response);
  }

  /**
   * 유효하지 않은 토큰 처리
   */
  private void handleInvalidToken(HttpServletResponse response, String token) throws IOException {
    if (CommonUtil.nvl(token, "").isEmpty()) { // 토큰 없음
      log.error("토큰이 존재하지 않습니다.");
      sendErrorResponse(response, ErrorCode.MISSING_AUTH_TOKEN);
    } else { // 유효하지 않은 토큰
      log.error("토큰이 유효하지 않습니다.");
      sendErrorResponse(response, ErrorCode.INVALID_JWT_TOKEN);
    }
  }

  /**
   * 요청 타입
   */
  private enum ApiRequestType {
    API, ADMIN, OTHER
  }
}
