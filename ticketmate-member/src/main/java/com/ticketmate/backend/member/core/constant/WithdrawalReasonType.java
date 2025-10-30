package com.ticketmate.backend.member.core.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReasonType {
  NO_CONCERTS("찾는 공연이 없어요"),
  RUDE_USER("비매너 사용자를 만났어요"),
  UNFAIR_RESTRICTION("억울하게 이용이 제한됐어요"),
  WANT_NEW_ACCOUNT("새 계정을 만들고 싶어요"),
  DELETE_PERSONAL_DATA("개인정보를 삭제하고 싶어요"),
  OTHER("기타"),
  ;

  private final String description;
}
