package com.ticketmate.backend.member.core.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MemberInfoConstants {

  // 닉네임 제약조건
  public static final int NICKNAME_MIN_LENGTH = 2;
  public static final int NICKNAME_MAX_LENGTH = 12;

  // 전화번호 제약조건
  public static final int PHONE_MAX_LENGTH = 13; // 010-1234-5678

}
