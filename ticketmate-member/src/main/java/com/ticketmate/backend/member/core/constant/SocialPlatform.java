package com.ticketmate.backend.member.core.constant;

import com.ticketmate.backend.common.core.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialPlatform {
  NORMAL, // 기본 로그인
  NAVER, // 네이버 로그인
  KAKAO, // 카카오 로그인
  GOOGLE; // 구글 로그인

  public static SocialPlatform fromRegistrationId(String id) {
    return CommonUtil.stringToEnum(SocialPlatform.class, id);
  }
}
