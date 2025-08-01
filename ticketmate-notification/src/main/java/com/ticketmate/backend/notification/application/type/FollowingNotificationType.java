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
public enum FollowingNotificationType implements DomainNotificationType {
  FOLLOW("팔로우", "{nickname} 님이 회원님을 팔로우하기 시작했습니다.");

  private final String title;
  private final String messageBody;

  /**
   * 팔로우 알림 Payload 생성
   */
  public NotificationPayload toPayload(String nickname) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(NotificationConstants.PLACEHOLDER_NICKNAME_KEY, nickname);
    return NotificationUtil.createNotification(this, placeholder);
  }
}
