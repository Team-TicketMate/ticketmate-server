package com.ticketmate.backend.object.constants.notification;

import java.util.Map;

public interface DomainNotificationType {
    String getTitle();  // 알림 제목 템플릿
    String getMessageBody();  // 내용 템플릿


    /**
     * title에 대한 치환
     */
    default String formatTitle(Map<String, String> placeholders) {
        return formatString(getTitle(), placeholders);
    }

    /**
     * messageBody에 대한 치환
     */
    default String formatMessage(Map<String, String> placeholders) {
        return formatString(getMessageBody(), placeholders);
    }

    /**
     * 여러 Placeholder를 치환해주는 메서드
     *  - {value} 변수들을 Map으로 받아 일괄 치환합니다.
     */
    private static String formatString(String template, Map<String, String> placeholders) {
        String result = template;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "알 수 없음" : entry.getValue();
            result = result.replace("{" + key + "}", value);
        }
        return result;
    }
}