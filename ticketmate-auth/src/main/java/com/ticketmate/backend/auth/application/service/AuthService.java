package com.ticketmate.backend.auth.application.service;

import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.ACCESS_TOKEN_KEY;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.REDIS_VERIFICATION_KEY;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.REFRESH_TOKEN_KEY;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.SMS_CODE_TTL_MIN;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.SMS_VERIFICATION_MESSAGE;
import static com.ticketmate.backend.common.core.util.CommonUtil.normalizeAndRemoveSpecialCharacters;

import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.core.service.TokenStore;
import com.ticketmate.backend.auth.infrastructure.properties.JwtProperties;
import com.ticketmate.backend.auth.infrastructure.util.AuthUtil;
import com.ticketmate.backend.auth.infrastructure.util.CookieUtil;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.infrastructure.domain.entity.Member;
import com.ticketmate.backend.sms.application.dto.SendCodeRequest;
import com.ticketmate.backend.sms.application.dto.VerifyCodeRequest;
import com.ticketmate.backend.sms.core.service.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final TokenProvider tokenProvider;
  private final TokenStore tokenStore;
  private final JwtProperties jwtProperties;
  private final MemberService memberService;
  private final SmsService smsService;
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 쿠키에 저장된 refreshToken을 통해 accessToken, refreshToken을 재발급합니다
   */
  @Transactional
  public void reissue(HttpServletRequest request, HttpServletResponse response) {

    log.debug("accessToken이 만료되어 재발급을 진행합니다.");

    // 쿠키에서 리프레시 토큰 추출 및 검증
    String refreshToken = AuthUtil.extractRefreshTokenFromRequest(request);

    // 사용자 정보 조회
    String memberId = tokenProvider.getMemberId(refreshToken);
    Member member = memberService.findMemberById(UUID.fromString(memberId));

    // 새로운 토큰 생성
    String newAccessToken = tokenProvider.createAccessToken(memberId, member.getUsername(), member.getRole().name());
    String newRefreshToken = tokenProvider.createRefreshToken(memberId, member.getUsername(), member.getRole().name());

    // 기존 refreshToken 삭제
    tokenStore.remove(refreshToken);

    // refreshToken 저장
    // RefreshToken을 Redis에 저장 (key: RT:memberId)
    tokenStore.save(AuthUtil.getRefreshTokenTtlKey(memberId), newRefreshToken, jwtProperties.refreshExpMillis());

    // 쿠키에 accessToken, refreshToken 추가
    response.addCookie(CookieUtil.createCookie(ACCESS_TOKEN_KEY, newAccessToken, jwtProperties.accessExpMillis() / 1000));
    response.addCookie(CookieUtil.createCookie(REFRESH_TOKEN_KEY, newRefreshToken, jwtProperties.refreshExpMillis() / 1000));
  }

  /**
   * 전화번호로 6자리 인증번호 생성 후 SMS 전송
   *
   * @param request phoneNumber 인증 전화번호
   */
  public void sendVerificationCode(SendCodeRequest request) {
    String code = generateCode(); // 6자리 인증코드 생성
    String normalizedPhoneNumber =
        normalizeAndRemoveSpecialCharacters(request.getPhoneNumber()); // 요청 전화번호 정규화 (01012345678)
    String key = generateKey(normalizedPhoneNumber); // Redis Key 생성
    saveCode(key, code); // Redis TTL 저장
    String message = generateMessage(code); // 인증문자 메시지 생성
    smsService.sendSms(normalizedPhoneNumber, message); // 인증문자 발송
  }

  /**
   * 본인인증 인증번호 검증
   *
   * @param request phoneNumber 인증 전화번호
   *                code 6자리 인증번호
   */
  public void verifyVerificationCode(VerifyCodeRequest request) {
    String normalizedPhoneNumber =
        normalizeAndRemoveSpecialCharacters(request.getPhoneNumber()); // 요청 전화번호 정규화 (01012345678)
    String key = generateKey(normalizedPhoneNumber); // Redis Key 생성
    String savedCode = getCode(key); // Redis에 저장된 인증코드 조회
    if (!savedCode.equals(request.getCode())) {
      log.error("인증번호가 일치하지 않습니다.");
      throw new CustomException(ErrorCode.VERIFY_CODE_NOT_SAME);
    }
    log.debug("본인인증 성공");
    deleteCode(key);
  }

  /**
   * Redis에 저장할 Key를 생성합니다 {VERIF_CODE:01012345678}
   *
   * @param phone 정규화된 전화번호 {01012345678}
   * @return VERIF_CODE:01012345678
   */
  private String generateKey(String phone) {
    return REDIS_VERIFICATION_KEY + phone;
  }

  /**
   * 6자리 인증 코드 생성
   */
  private String generateCode() {
    SecureRandom secureRandom = new SecureRandom();
    int number = secureRandom.nextInt(100_000, 1_000_000); // 6자리 정수 생성
    log.debug("6자리 인증코드 생성 성공");
    return String.valueOf(number);
  }

  /**
   * Redis TTL [Key: VERIF_CODE:01012345678] 인증코드 저장
   *
   * @param key  Redis TTL Key
   * @param code 6자리 인증코드
   */
  private void saveCode(String key, String code) {
    log.debug("Redis에 저장된 TTL Key: {}", key);
    redisTemplate.opsForValue()
        .set(key, code, SMS_CODE_TTL_MIN, TimeUnit.MINUTES);
  }

  /**
   * Redis에서 저장된 인증번호 조회
   *
   * @param key Redis TTL Key
   * @return Redis에 저장된 인증번호
   */
  private String getCode(String key) {
    String code = redisTemplate.opsForValue().get(key);
    if (CommonUtil.nvl(code, "").isEmpty()) {
      log.error("인증번호가 만료되었거나, 존재하지 않습니다.");
      throw new CustomException(ErrorCode.VERIFY_CODE_EXPIRED_OR_NOT_FOUND);
    }
    return code;
  }

  /**
   * Redis에 저장된 인증번호 삭제
   *
   * @param key Redis TTL Key
   */
  private void deleteCode(String key) {
    redisTemplate.delete(key);
    log.debug("Redis에 저장된 인증번호 삭제 성공: {}", key);
  }

  /**
   * 인증 문자 텍스트 생성
   *
   * @return [Ticketmate] 인증번호는 OOOOOO 입니다.
   */
  private String generateMessage(String code) {
    String message = SMS_VERIFICATION_MESSAGE.replace("{code}", code);
    log.debug("인증 문자 텍스트 생성: {}", message);
    return message;
  }
}
