package com.ticketmate.backend.auth.infrastructure.oauth2.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 소셜로그인 개발문서
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info-response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoApiResponse(
  @JsonProperty("id")
  Long id,
  @JsonProperty("kakao_account")
  KakaoAccount kakaoAccount
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record KakaoAccount(
    @JsonProperty("name")
    String name,
    @JsonProperty("email")
    String email,
    @JsonProperty("birthyear")
    String birthYear,
    @JsonProperty("birthday")
    String birthDay,
    @JsonProperty("gender")
    String gender,
    @JsonProperty("phone_number")
    String phoneNumber
  ) {

  }
}
