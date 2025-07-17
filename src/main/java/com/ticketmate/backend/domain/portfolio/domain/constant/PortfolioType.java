package com.ticketmate.backend.domain.portfolio.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PortfolioType {

  PENDING_REVIEW("검토 대기"),  // 관리자가 아직 보지않은 포트폴리오 (최초 업로드시 모두 해당상태로 저장됩니다.)

  REVIEWING("검토 중"),  // 현재 관리자가 검토중인 포트폴리오

  APPROVED("승인"),  // 관리자가 승인해 의뢰인 -> 대리인으로 바뀐 포트폴리오

  REJECTED("반려");  // 관리자에게 반려당한 포트폴리오

  private final String description;
}
