package com.ticketmate.backend.domain.notification.domain.constant;

public interface DomainNotificationType {

  String getTitle();  // 알림 제목 템플릿

  String getMessageBody();  // 내용 템플릿
}