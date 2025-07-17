package com.ticketmate.backend.domain.auth.domain.dto.response;

import com.ticketmate.backend.domain.member.domain.constant.SocialPlatform;

public interface OAuth2Response {

  SocialPlatform getSocialPlatform(); // 소셜 플랫폼

  String getId(); // PK

  String getName(); // 사용자 이름

  String getEmail(); // 이메일

  String getGender(); // 성별

  String getBirthDay(); // 사용자 생일

  String getBirthYear(); // 출생연도

  String getPhone(); // 전화번호
}
