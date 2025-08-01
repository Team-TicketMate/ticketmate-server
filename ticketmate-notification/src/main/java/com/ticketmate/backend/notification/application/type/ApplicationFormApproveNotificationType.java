package com.ticketmate.backend.notification.application.type;

import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.application.util.NotificationUtil;
import com.ticketmate.backend.notification.core.constant.NotificationConstants;
import com.ticketmate.backend.notification.core.type.DomainNotificationType;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationFormApproveNotificationType implements DomainNotificationType {

  /*
  ======================================신청폼 수락======================================
  */
  APPROVE("신청서가 수락됐습니다!", "대리인 {nickname} 님께서 의뢰인님의 티켓팅을 수락했습니다!");

  private final String title;
  private final String messageBody;

  /**
   * 신청서 승인 알림 payload 생성
   */
  public NotificationPayload toPayload(String nickname) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(NotificationConstants.PLACEHOLDER_NICKNAME_KEY, nickname);
    return NotificationUtil.createNotification(this, placeholder);
  }
}
