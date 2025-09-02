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
public enum PortfolioNotificationType implements DomainNotificationType {
  /*
  ======================================포트폴리오======================================
  */
  REVIEWING("포트폴리오 검토중", "관리자가 {nickname} 님의 포트폴리오를 검토중입니다."),

  APPROVED("포트폴리오 승인 완료", "{nickname} 님의 포트폴리오가 관리자에 의해 승인처리 되었습니다."),

  REJECTED("포트폴리오 반려", "{nickname} 님의 포트폴리오가 관리자에 의해 반려처리 되었습니다.");

  private final String title;
  private final String messageBody;

  /**
   * 포트폴리오 알림 payload 생성
   */
  public NotificationPayload toPayload(String nickname) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(NotificationConstants.PLACEHOLDER_NICKNAME_KEY, nickname);
    return NotificationUtil.createNotification(this, placeholder);
  }
}