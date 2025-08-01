package com.ticketmate.backend.notification.application.util;

import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.core.type.DomainNotificationType;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NotificationUtil {

  /**
   * 범용 알림 생성 메서드
   */
  public NotificationPayload createNotification(DomainNotificationType type, Map<String, String> placeholder) {
    return NotificationPayload.builder()
        .title(format(type.getTitle(), placeholder))
        .body(format(type.getMessageBody(), placeholder))
        .build();
  }

  /**
   * 템플릿의 {key}를 placeholders 에서 찾아 치환
   *
   * @param template     원본 템플릿 (ex. "안녕하세요 {nickname}님")
   * @param placeholders key-value 치환 Map
   * @return 치환된 최종 문자열
   */
  public String format(String template, Map<String, String> placeholders) {
    String result = template;

    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue() == null ? "알 수 없음" : entry.getValue();
      result = result.replace("{" + key + "}", value);
    }
    return result;
  }
}