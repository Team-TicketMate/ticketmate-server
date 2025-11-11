package com.ticketmate.backend.auth.infrastructure.oauth2.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmate.backend.auth.core.profile.OAuth2Profile;
import com.ticketmate.backend.auth.infrastructure.oauth2.response.KakaoApiResponse;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record KakaoOAuth2Profile(KakaoApiResponse response) implements OAuth2Profile {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public KakaoOAuth2Profile(Map<String, Object> attributes) {
    this(MAPPER.convertValue(attributes, KakaoApiResponse.class));
  }

  @Override
  public SocialPlatform getSocialPlatform() {
    return SocialPlatform.KAKAO;
  }

  @Override
  public String getId() {
    log.debug("카카오 소셜 로그인 id: {}", response.id());
    return response.id().toString(); // 회원 번호 (Long -> String)
  }

  @Override
  public String getName() {
    String name = response.kakaoAccount().name();
    log.debug("카카오 소셜 로그인 name: {}", name);
    return name; // 카카오계정 이름
  }

  @Override
  public String getEmail() {
    String email = response.kakaoAccount().email();
    log.debug("카카오 소셜 로그인 email: {}", email);
    return email; // 카카오계정 대표 이메일
  }

  @Override
  public String getGender() {
    String gender = response.kakaoAccount().gender();
    log.debug("카카오 소셜 로그인 gender: {}", gender);
    return gender; // 성별 female: 여성, male: 남성
  }

  @Override
  public String getBirthDay() {
    String birthDay = response.kakaoAccount().birthDay();
    log.debug("카카오 소셜 로그인 birthday: {}", birthDay);
    return birthDay; // 생일 (MMDD 형식)
  }

  @Override
  public String getBirthYear() {
    String birthYear = response.kakaoAccount().birthYear();
    log.debug("카카오 소셜 로그인 birthyear: {}", birthYear);
    return birthYear; // 출생 연도 (YYYY 형식)
  }

  @Override
  public String getPhone() {
    String phone = response.kakaoAccount().phoneNumber();
    log.debug("카카오 소셜 로그인 phone_number: {}", phone);
    return phone;
  }
}
