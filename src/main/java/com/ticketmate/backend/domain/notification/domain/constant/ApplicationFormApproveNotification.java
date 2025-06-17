package com.ticketmate.backend.domain.notification.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationFormApproveNotification implements DomainNotificationType {
  /*
======================================신청폼 수락======================================
*/
  APPROVE("신청서가 수락됐습니다!", "대리인 {nickname} 님께서 의뢰인님의 티켓팅을 수락했습니다!");

  private final String title;
  private final String messageBody;
}
