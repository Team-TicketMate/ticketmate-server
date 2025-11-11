package com.ticketmate.backend.auth.infrastructure.oauth2.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 네이버 소셜로그인 개발문서
 * https://developers.naver.com/docs/login/devguide/devguide.md#3-4-5-%EC%A0%91%EA%B7%BC-%ED%86%A0%ED%81%B0%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%98%EC%97%AC-%ED%94%84%EB%A1%9C%ED%95%84-api-%ED%98%B8%EC%B6%9C%ED%95%98%EA%B8%B0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverApiResponse(
  @JsonProperty("response")
  Response response
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Response(
    @JsonProperty("id")
    String id,
    @JsonProperty("name")
    String name,
    @JsonProperty("email")
    String email,
    @JsonProperty("gender")
    String gender,
    @JsonProperty("birthday")
    String birthDay,
    @JsonProperty("birthyear")
    String birthYear,
    @JsonProperty("mobile")
    String phone
  ) {


  }

}
