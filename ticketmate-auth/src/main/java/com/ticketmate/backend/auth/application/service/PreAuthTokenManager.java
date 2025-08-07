package com.ticketmate.backend.auth.application.service;

import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.PRE_AUTH_KEY_PREFIX;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.PRE_AUTH_TTL_MIN;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 관리자 로그인 후 pre-auth 단계에서 사용할 토큰 생성 & 조회 & 삭제
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PreAuthTokenManager {

  private final RedisTemplate<String, String> redisTemplate;
  private final MemberService memberService;

  /**
   * PreAuthToken 생성 및 TTL 저장
   *
   * @param member PreAuthToken 발급 대상 사용자
   * @return PreAuthToken
   */
  public String generatePreAuthToken(Member member) {
    log.debug("PreAuthToken 생성: 관리자: {}", member.getName());
    String token = UUID.randomUUID().toString();
    String key = PRE_AUTH_KEY_PREFIX + token;
    redisTemplate.opsForValue().set(key, member.getMemberId().toString(), PRE_AUTH_TTL_MIN, TimeUnit.MINUTES);
    return token;
  }

  /**
   * PreAuthToken 으로 Member 조회
   */
  public Member getMemberByPreAuthToken(String preAuthToken) {
    String key = PRE_AUTH_KEY_PREFIX + preAuthToken;
    String memberId = redisTemplate.opsForValue().get(key);
    if (CommonUtil.nvl(memberId, "").isEmpty()) {
      log.error("preAuthToken이 만료되었거나, 토큰에 해당하는 사용자를 찾을 수 없습니다.");
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }
    return memberService.findMemberById(UUID.fromString(memberId));
  }

  /**
   * PreAuthToken 사용 완료 후 삭제
   */
  public void deletePreAuthToken(String preAuthToken) {
    log.debug("PreAuthToken 삭제");
    redisTemplate.delete(PRE_AUTH_KEY_PREFIX + preAuthToken);
  }

}
