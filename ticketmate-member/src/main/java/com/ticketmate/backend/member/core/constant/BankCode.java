package com.ticketmate.backend.member.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BankCode {
  KYONGNAM_BANK("039", "경남"),
  GWANGJU_BANK("034", "광주"),
  LOCALNONGHYEOP("012", "지역축농협"),
  BUSAN_BANK("032", "부산"),
  SAEMAUL("045", "새마을"),
  SANLIM("064", "산림"),
  SHINHAN("088", "신한"),
  SHINHYEOP("048", "신협"),
  CITI("027", "씨티"),
  WOORI("020", "우리"),
  POST("071", "우체국"),
  SAVING_BANK("050", "저축"),
  JEONBUK_BANK("037", "전북"),
  JEJU_BANK("035", "제주"),
  KAKAO_BANK("090", "카카오"),
  K_BANK("089", "케이"),
  TOSS_BANK("092", "토스"),
  HANA("081", "하나"),
  HSBC("054", "홍콩상하이"),
  IBK("003", "기업"),
  KOOKMIN("004", "국민"),
  DAEGU_BANK("031", "대구"),
  KDB_BANK("002", "산업"),
  NONGHYEOP("011", "농협"),
  SC("023", "SC제일"),
  SUHYEOP("007", "수협");

  private final String kttcCode;  // 금융결제원 공식 코드
  private final String displayName;  // 화면 표기 기본값
}
