package com.ticketmate.backend.object.dto.auth.response;

import com.ticketmate.backend.object.constants.SocialPlatform;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 소셜로그인 개발문서
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info-response
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class KakaoResponse implements OAuth2Response {

  private final Map<String, Object> attribute;

  @Override
  public SocialPlatform getSocialPlatform() {
    return SocialPlatform.KAKAO;
  }

  @Override
  public String getId() {
    log.debug("카카오 소셜 로그인 id: {}", attribute.get("id"));
    return attribute.get("id").toString(); // Long.toString 회원번호
  }

  @Override
  public String getName() {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    log.debug("카카오 소셜 로그인 name: {}", kakaoAccount.get("name"));
    return kakaoAccount.get("name").toString(); // 카카오계정 이름
  }

  @Override
  public String getEmail() {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    log.debug("카카오 소셜 로그인 email: {}", kakaoAccount.get("email"));
    return kakaoAccount.get("email").toString(); // 카카오계정 대표 이메일
  }

  @Override
  public String getGender() {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    log.debug("카카오 소셜 로그인 gender: {}", kakaoAccount.get("gender"));
    return kakaoAccount.get("gender").toString(); // 성별 female: 여성, male: 남성
  }

  @Override
  public String getBirthDay() {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    log.debug("카카오 소셜 로그인 birthday: {}", kakaoAccount.get("birthday"));
    return kakaoAccount.get("birthday").toString(); // 생일 (MMDD 형식)
  }

  @Override
  public String getBirthYear() {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    log.debug("카카오 소셜 로그인 birthyear: {}", kakaoAccount.get("birthyear"));
    return kakaoAccount.get("birthyear").toString(); // 출생 연도 (YYYY 형식)
  }

  @Override
  public String getPhone() {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    String phoneNumber = kakaoAccount.get("phone_number").toString(); // 카카오 계정 전화번호: 국내번호 +82 00-0000-0000
    log.debug("카카오 소셜 로그인 phone_number: {}", phoneNumber);
    if (phoneNumber.startsWith("+82 ")) {
      phoneNumber = phoneNumber.replace("+82 ", "0");
    }
    return phoneNumber;
  }
}
