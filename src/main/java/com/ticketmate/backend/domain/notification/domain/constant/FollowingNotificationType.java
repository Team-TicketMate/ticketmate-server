package com.ticketmate.backend.domain.notification.domain.constant;

import static com.ticketmate.backend.global.constant.NotificationConstants.PLACEHOLDER_NICKNAME_KEY;

import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayload;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
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
  public NotificationPayload toPayload(Member follower) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(PLACEHOLDER_NICKNAME_KEY, follower.getNickname());
    return NotificationUtil.createNotification(this, placeholder);
  }
}
