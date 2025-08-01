package com.ticketmate.backend.notification.core.type;

public interface DomainNotificationType {

  String getTitle();  // 알림 제목 템플릿

  String getMessageBody();  // 내용 템플릿
}