package com.ticketmate.backend.notification.core.service;

import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  /**
   * 단일 사용자 알림 전송
   *
   * @param memberId 전송 대상 회원 PK
   * @param payload  알림 제목/본문 페이로드
   */
  void sendToMember(UUID memberId, NotificationPayload payload);

  /**
   * 다수 사용자 알림 전송
   *
   * @param memberIdList 전송 대상 회원 PK 리스트
   * @param payload      알림 제목/본문 페이로드
   */
  void sendToMemberList(List<UUID> memberIdList, NotificationPayload payload);

  /**
   * 전체 사용자 알림 전송
   *
   * @param payload 알림 페이로드
   */
  void sendToAll(NotificationPayload payload);
}
