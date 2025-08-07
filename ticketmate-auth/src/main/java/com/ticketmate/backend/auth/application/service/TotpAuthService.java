package com.ticketmate.backend.auth.application.service;

import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.TOTP_PENDING_SECRET_KEY_PREFIX;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.TOTP_PENDING_SECRET_TTL_MIN;

import com.ticketmate.backend.auth.core.dto.TokenPair;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import com.ticketmate.backend.totp.application.dto.request.TotpVerifyRequest;
import com.ticketmate.backend.totp.application.dto.response.TotpSetupResponse;
import com.ticketmate.backend.totp.core.service.TotpService;
import com.ticketmate.backend.totp.core.value.TotpCredentials;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * TOTP 설정 & 검증 & 리셋
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TotpAuthService {

  private final PreAuthTokenManager preAuthTokenManager;
  private final RedisTemplate<String, String> redisTemplate;
  private final TotpService totpService;
  private final MemberRepository memberRepository;
  private final JwtManager jwtManager;

  /**
   * TOTP 등록
   * - Google Authenticator에 MFA 등록을 위한 QR코드 및 AuthURL 생성
   * - Redis에 임시 시크릿 저장 및 반환
   */
  @Transactional
  public TotpSetupResponse setupTotp(String preAuthToken) {
    Member member = preAuthTokenManager.getMemberByPreAuthToken(preAuthToken);
    ensureTotpNotEnabled(member);
    String key = TOTP_PENDING_SECRET_KEY_PREFIX + member.getMemberId();
    String secret = redisTemplate.opsForValue().get(key);
    TotpCredentials totpCredentials;
    if (secret == null) {
      log.debug("새로운 TOTP secret를 발급 및 저장합니다.");
      totpCredentials = totpService.generateCredentials(member.getUsername());
      secret = totpCredentials.secret();
      redisTemplate.opsForValue().set(key, secret, TOTP_PENDING_SECRET_TTL_MIN, TimeUnit.MINUTES);
    } else {
      log.debug("기존에 발급된 TOTP secret를 조회합니다.");
      String otpAuthUrl = totpService.getOtpAuthUrl(member.getUsername(), secret);
      totpCredentials = new TotpCredentials(secret, otpAuthUrl);
    }
    return new TotpSetupResponse(totpCredentials.secret(), totpCredentials.otpAuthUrl());
  }

  /**
   * TOTP 등록 검증
   * - TOTP 최초 등록 시 사용
   * - Redis에서 PENDING 시크릿 조회 -> 코드 검증 -> 2FA 등록 활성화 -> TOTP 시크릿 DB 저장
   *
   * @param preAuthToken TOTP 최초 등록 사용자 preAuthToken
   * @param request      TOTP 코드
   */
  @Transactional
  public void verifySetupTotp(String preAuthToken, TotpVerifyRequest request) {
    log.debug("초기 TOTP 검증 및 활성화");
    Member member = preAuthTokenManager.getMemberByPreAuthToken(preAuthToken);
    ensureTotpNotEnabled(member);
    String key = TOTP_PENDING_SECRET_KEY_PREFIX + member.getMemberId();
    String pendingSecret = redisTemplate.opsForValue().get(key);
    if (CommonUtil.nvl(pendingSecret, "").isEmpty()) {
      log.error("관리자: {} 에 대한 TOTP pending secret이 존재하지 않습니다.", member.getName());
      throw new CustomException(ErrorCode.PENDING_TOTP_SECRET_NOT_FOUND);
    }
    if (!totpService.verifyCode(pendingSecret, request.getCode())) {
      log.error("관리자: {} TOTP 인증에 실패했습니다", member.getName());
      throw new CustomException(ErrorCode.INVALID_TOTP_CODE);
    }
    member.setTotpEnabled(true);
    member.setTotpSecret(pendingSecret);
    memberRepository.save(member);
    redisTemplate.delete(key);
  }

  /**
   * 로그인 시 TOTP 코드가 유효한지 검증
   * - DB에 저장된 totpSecret으로 코드 검증
   *
   * @param preAuthToken 로그인 사용자 preAuthToken
   * @param request      TOTP 코드
   */
  @Transactional
  public boolean verifyLoginTotp(String preAuthToken, TotpVerifyRequest request, HttpServletResponse response) {
    log.debug("관리자 로그인 TOTP 2차인증 코드 검증");
    Member member = preAuthTokenManager.getMemberByPreAuthToken(preAuthToken);
    ensureTotpEnabled(member);
    if (totpService.verifyCode(member.getTotpSecret(), request.getCode())) {
      log.debug("관리자 2차인증 성공: 엑세스 토큰 및 리프레시 토큰 생성");
      TokenPair tokenPair = jwtManager.generateTokenPair(member);
      jwtManager.saveAndAttachTokenPair(member, tokenPair, response);
      return true;
    } else {
      log.error("2FA 인증 실패");
      return false;
    }
  }

  /**
   * TOTP 리셋
   * - Member 테이블에 저장된 시크릿 키 제거
   * - Member totpEnabled = false 설정
   *
   * @param preAuthToken TOTP 리셋할 사용자 preAuthToken
   */
  @Transactional
  public void resetTotp(String preAuthToken) {
    Member member = preAuthTokenManager.getMemberByPreAuthToken(preAuthToken);
    ensureTotpEnabled(member);
    member.setTotpEnabled(false);
    member.setTotpSecret(null);
    memberRepository.save(member);
  }

  /**
   * TOTP가 활성화 되어있는지 검증
   */
  private void ensureTotpEnabled(Member member) {
    if (!member.isTotpEnabled()) {
      log.error("TOTP가 활성화되어있지 않은 관리자 입니다. 관리자: {}", member.getName());
      throw new CustomException(ErrorCode.TOTP_NOT_ENABLED);
    }
  }

  /**
   * TOTP가 활성화 되어있지 않은지 검증
   */
  private void ensureTotpNotEnabled(Member member) {
    if (member.isTotpEnabled()) {
      log.error("이미 TOTP가 활성화되어있는 관리자 입니다. 관리자: {}", member.getName());
      throw new CustomException(ErrorCode.TOTP_ALREADY_ENABLED);
    }
  }
}
