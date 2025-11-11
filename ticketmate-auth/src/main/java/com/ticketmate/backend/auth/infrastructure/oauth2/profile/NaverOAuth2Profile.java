package com.ticketmate.backend.auth.infrastructure.oauth2.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmate.backend.auth.core.profile.OAuth2Profile;
import com.ticketmate.backend.auth.infrastructure.oauth2.response.NaverApiResponse;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record NaverOAuth2Profile(NaverApiResponse response) implements OAuth2Profile {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public NaverOAuth2Profile(Map<String, Object> attributes) {
    this(MAPPER.convertValue(attributes, NaverApiResponse.class));
  }

  @Override
  public SocialPlatform getSocialPlatform() {
    return SocialPlatform.NAVER; // 소셜 플랫폼 종류
  }

  @Override
  public String getId() {
    String id = response.response().id();
    log.debug("네이버 소셜 로그인 id: {}", id);
    return id; // 네이버 PK (네이버 아이디마다 고유하게 발급)
  }

  @Override
  public String getName() {
    String name = response.response().name();
    log.debug("네이버 소셜 로그인 name: {}", name);
    return name; // 사용자 이름
  }

  @Override
  public String getEmail() {
    String email = response.response().email();
    log.debug("네이버 소셜 로그인 email: {}", email);
    return email; // 사용자 메일 주소
  }

  @Override
  public String getGender() {
    String gender = response.response().gender(); // 성별 F:여성, M:남성, U:확인불가
    log.debug("네이버 소셜 로그인 gender: {}", gender);
    if (gender.equals("F")) {
      gender = "female";
    } else if (gender.equals("M")) {
      gender = "male";
    }
    return gender; // female, male, U 반환 (카카오와 형식 통일) TODO: 추후 VO 별도 분리
  }

  @Override
  public String getBirthDay() {
    String birthDay = response.response().birthDay();
    log.debug("네이버 소셜 로그인 birthday: {}", birthDay); // 사용자 생일 (MM-DD 형식)
    birthDay = birthDay.replace("-", ""); // TODO: 추후 VO 별도 분리
    return birthDay; // MMDD 형식 반환
  }

  @Override
  public String getBirthYear() {
    String birthYear = response.response().birthYear();
    log.debug("네이버 소셜 로그인 birthyear: {}", birthYear);
    return birthYear; // 출생연도
  }

  @Override
  public String getPhone() {
    String phone = response.response().phone();
    log.debug("네이버 소셜 로그인 mobile: {}", phone);
    return phone; // 휴대전화번호
  }
}
