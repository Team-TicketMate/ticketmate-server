package com.ticketmate.backend.util.notification;

import com.ticketmate.backend.object.constants.NotificationType;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.dto.notification.request.NotificationPayloadRequest;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    /**
     * 포트폴리오 관련 알림내용을 만들어주는 메서드입니다.
     */
    public NotificationPayloadRequest portfolioNotification(PortfolioType portfolioType, Portfolio portfolio) {
        if (portfolioType == null || portfolio == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 매핑 정보에 따라 NotificationType 구하기
        NotificationType type = NotificationType.from(portfolioType);
        if (type == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 알림 메시지 생성
        String nickname = portfolio.getMember().getNickname();
        String body = type.formatMessage(nickname);

        return NotificationPayloadRequest.builder()
                .body(body)
                .title(type.getTitle())
                .build();
    }
}
