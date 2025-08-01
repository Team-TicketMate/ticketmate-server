package com.ticketmate.backend.notification.infrastructure.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.core.constant.NotificationConstants;
import com.ticketmate.backend.notification.core.service.NotificationService;
import com.ticketmate.backend.notification.infrastructure.entity.FcmToken;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Qualifier("web")
@Slf4j
@RequiredArgsConstructor
public class WebNotificationService implements NotificationService {

  private final FcmTokenService fcmTokenService;

  /**
   * 단일 사용자 알림 전송 (Web)
   *
   * @param memberId 전송 대상 회원 PK
   * @param payload  알림 제목/본문 페이로드
   */
  @Override
  @Transactional
  public void sendToMember(UUID memberId, NotificationPayload payload) {
    List<FcmToken> fcmTokenList = fcmTokenService.findAllTokensByMemberId(memberId);
    if (CommonUtil.nullOrEmpty(fcmTokenList)) {
      log.debug("회원: {}에 대해 저장된 FCM 토큰이 없습니다", memberId);
      return;
    }
    fcmTokenList.forEach(token -> sendNotification(token, payload));
  }

  /**
   * 다수 사용자 알림 전송 (Web)
   *
   * @param memberIdList 전송 대상 회원 PK 리스트
   * @param payload      알림 제목/본문 페이로드
   */
  @Override
  @Transactional
  public void sendToMemberList(List<UUID> memberIdList, NotificationPayload payload) {
    for (UUID memberId : memberIdList) {
      sendToMember(memberId, payload);
    }
  }

  /**
   * 전체 사용자 알림 전송 (Web)
   *
   * @param payload 알림 페이로드
   */
  @Override
  @Transactional
  public void sendToAll(NotificationPayload payload) {
    fcmTokenService.findAllTokens()
        .forEach(fcmToken -> sendNotification(fcmToken, payload));
  }

  /**
   * FCM Webpush 알림 전송
   *
   * @param fcmToken Web 대상 토큰 정보
   * @param request  알림 제목/본문/아이콘 DTO
   */
  private void sendNotification(FcmToken fcmToken, NotificationPayload request) {
    try {
      WebpushNotification webpushNotification = WebpushNotification.builder()
          .setTitle(request.getTitle())
          .setBody(request.getBody())
          .setIcon(NotificationConstants.NOTIFICATION_ICON_PATH)
          .build();

      WebpushConfig webpushConfig = WebpushConfig.builder()
          .setNotification(webpushNotification)
          .putHeader(NotificationConstants.NOTIFICATION_TTL_PREFIX, NotificationConstants.NOTIFICATION_TTL)
          .build();

      Message message = Message.builder()
          .setToken(fcmToken.getToken())
          .setWebpushConfig(webpushConfig)
          .build();

      String response = FirebaseMessaging.getInstance().send(message);
      log.debug("알림 전송 성공. 응답: {}", response);
    } catch (Exception e) {
      log.error("웹 푸시 알림 전송 중 오류 발생: ", e);
    }
  }
}
