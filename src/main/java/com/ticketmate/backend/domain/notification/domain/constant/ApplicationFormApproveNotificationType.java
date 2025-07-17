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
  public NotificationPayload toPayload(Member agent) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(PLACEHOLDER_NICKNAME_KEY, agent.getNickname());
    return NotificationUtil.createNotification(this, placeholder);
  }
}
