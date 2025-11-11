package com.ticketmate.backend.mock.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;

import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.mock.application.dto.request.MockNotificationRequest;
import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockNotificationFactory {

  private final NotificationService notificationService;

  /**
   * 테스트 알림 발송
   */
  public void sendTestNotification(Member member, MockNotificationRequest request) {
    String title = nvl(request.getTitle(), "").isEmpty() ? "테스트 알림입니다." : request.getTitle();
    String body = nvl(request.getBody(), "").isEmpty() ? "테스트 본문입니다." : request.getBody();
    NotificationPayload payload = NotificationPayload.builder()
      .title(title)
      .body(body)
      .build();
    log.debug("테스트 알림 발송: {}", member.getMemberId());
    notificationService.sendToMember(member.getMemberId(), payload);
  }
}
