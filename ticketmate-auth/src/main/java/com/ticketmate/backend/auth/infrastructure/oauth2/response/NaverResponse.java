package com.ticketmate.backend.auth.infrastructure.oauth2.response;

import com.ticketmate.backend.auth.core.response.OAuth2Response;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 네이버 소셜로그인 개발문서
 * https://developers.naver.com/docs/login/devguide/devguide.md#3-3-1-%EB%84%A4%EC%9D%B4%EB%B2%84-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%ED%9A%8C%EC%9B%90%EC%9D%98-%ED%94%84%EB%A1%9C%ED%95%84-%EC%A0%95%EB%B3%B4
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class NaverResponse implements OAuth2Response {

  private final Map<String, Object> attribute;

  @Override
  public SocialPlatform getSocialPlatform() {
    return SocialPlatform.NAVER; // 소셜 플랫폼 종류
  }

  @Override
  public String getId() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    log.debug("네이버 소셜 로그인 id: {}", response.get("id"));
    return response.get("id").toString(); // 네이버 PK (네이버 아이디마다 고유하게 발급)
  }

  @Override
  public String getName() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    log.debug("네이버 소셜 로그인 name: {}", response.get("name"));
    return response.get("name").toString(); // 사용자 이름
  }

  @Override
  public String getEmail() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    log.debug("네이버 소셜 로그인 email: {}", response.get("email"));
    return response.get("email").toString(); // 사용자 메일 주소
  }

  @Override
  public String getGender() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    String gender = response.get("gender").toString(); // 성별 F:여성, M:남성, U:확인불가
    log.debug("네이버 소셜 로그인 gender: {}", gender);
    if (gender.equals("F")) {
      gender = "female";
    } else if (gender.equals("M")) {
      gender = "male";
    }
    return gender; // female, male, U 반환 (카카오와 형식 통일)
  }

  @Override
  public String getBirthDay() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    String birthday = response.get("birthday").toString();
    log.debug("네이버 소셜 로그인 birthday: {}", birthday); // 사용자 생일 (MM-DD 형식)
    birthday = birthday.replace("-", "");
    return birthday; // MMDD 형식 반환
  }

  @Override
  public String getBirthYear() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    log.debug("네이버 소셜 로그인 birthyear: {}", response.get("birthyear"));
    return response.get("birthyear").toString(); // 출생연도
  }

  @Override
  public String getPhone() {
    Map<String, Object> response = (Map<String, Object>) attribute.get("response");
    log.debug("네이버 소셜 로그인 mobile: {}", response.get("mobile"));
    return response.get("mobile").toString(); // 휴대전화번호
  }
}
