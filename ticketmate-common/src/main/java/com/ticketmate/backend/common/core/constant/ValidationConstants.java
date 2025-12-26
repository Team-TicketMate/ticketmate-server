package com.ticketmate.backend.common.core.constant;

import java.time.Duration;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationConstants {

  @UtilityClass
  public static class Member {

    // 닉네임 제약조건
    public static final int NICKNAME_MIN_LENGTH = 2;
    public static final int NICKNAME_MAX_LENGTH = 12;

    // 전화번호 제약조건
    public static final int PHONE_MAX_LENGTH = 13; // +8212345678 (E.164 형식)

    // 생일 및 출생연도 제약조건
    public static final int BIRTHDAY_LENGTH = 4; // MMDD
    public static final int BIRTHYEAR_LENGTH = 4; // YYYY

    // 자기소개 제약조건
    public static final int INTRODUCTION_MAX_LENGTH = 50;
  }

  @UtilityClass
  public static class ApplicationForm {

    // 우선순위 제약조건
    public static final int PRIORITY_MIN_VALUE = 1;
    public static final int PRIORITY_MAX_VALUE = 5;

    // 요청 매수 제약조건
    public static final int APPLICATION_FORM_MIN_REQUEST_COUNT = 1;
    public static final int APPLICATION_FORM_MAX_REQUEST_COUNT = 10;

    // 요청 사항 최대 길이
    public static final int REQUIREMENT_MAX_LENGTH = 100;

    // 희망구역 최대 개수
    public static final int HOPE_AREA_MAX_SIZE = 5;

  }

  @UtilityClass
  public static class Portfolio {

    // 포트폴리오 글자 수 제약조건
    public static final int PORTFOLIO_DESCRIPTION_MIN_LENGTH = 20;
    public static final int PORTFOLIO_DESCRIPTION_MAX_LENGTH = 200;

    // 포트폴리오 이미지 제약조건
    public static final int PORTFOLIO_IMG_MIN_COUNT = 1;
    public static final int PORTFOLIO_IMG_MAX_COUNT = 20;
  }

  @UtilityClass
  public static class Chat {

    public static final int CHAT_MESSAGE_MAX_LENGTH = 500;
    public static final int CHAT_IMG_MAX_COUNT = 3;

    public static final int SEARCH_KEYWORD_MAX_LENGTH = 30;
  }

  @UtilityClass
  public static class Search {

    public static final int KEYWORD_MAX_LENGTH = 20;
  }

  @UtilityClass
  public static class AgentBankAccount {

    public static final int MAX_ACCOUNT_COUNT = 5;
    public static final int MAX_ACCOUNT_HOLDER_LENGTH = 20;
  }

  @UtilityClass
  public static class MemberWithdrawal {

    public static final Duration WITHDRAW_BLOCK_DURATION = Duration.ofDays(30);
    public static final int WITHDRAW_OTHER_REASON_MAX_LENGTH = 20;
  }

  @UtilityClass
  public static class FullfillmentForm {

    // 성공 사진 이미지 최대 개수
    public static final int FULLFILLMENT_IMG_MAX_COUNT = 6;
    // 상세 설명 최대 길이
    public static final int PARTICULAR_MEMO_MAX_LENGTH = 100;
    // 거절 사유 최대 길이
    public static final int REJECTED_MEMO_MAX_LENGTH = 100;
  }

  @UtilityClass
  public static class Review {

    public static final int REVIEW_IMG_MAX_COUNT = 3;

    public static final int COMMENT_MIN_LENGTH = 10;
    public static final int COMMENT_MAX_LENGTH = 300;

    public static final double RATING_MIN = 0.0;
    public static final double RATING_MAX = 5.0;
  }
}
