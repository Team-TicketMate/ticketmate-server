package com.ticketmate.backend.domain.member.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialPlatform {
  NORMAL, // 기본 로그인
  NAVER, // 네이버 로그인
  KAKAO, // 카카오 로그인
  GOOGLE; // 구글 로그인
}
