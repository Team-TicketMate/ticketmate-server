package com.ticketmate.backend.util.notification;

import com.ticketmate.backend.object.constants.ApplicationFormRejectedType;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.constants.notification.ApplicationFormRejectNotificationType;
import com.ticketmate.backend.object.constants.notification.PortfolioNotificationType;
import com.ticketmate.backend.object.dto.notification.request.NotificationPayloadRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationUtil {
    private static final String PLACE_HOLDER_NICK_NAME_KEY = "nickname";
    private static final String PLACE_HOLDER_REJECT_OTHER_MEMO = "otherReject";

    /**
     * 포트폴리오 관련 알림내용을 만들어주는 메서드입니다.
     */
    public NotificationPayloadRequest portfolioNotification(PortfolioType portfolioType, Portfolio portfolio) {
        if (portfolioType == null || portfolio == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 매핑 정보에 따라 NotificationType 구하기
        PortfolioNotificationType type = PortfolioNotificationType.from(portfolioType);
        if (type == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 알림 메시지 생성
        Map<String, String> placeHolder = new HashMap<>();
        String nickname = portfolio.getMember().getNickname();
        placeHolder.put(PLACE_HOLDER_NICK_NAME_KEY, nickname);
        String body = type.formatMessage(placeHolder);

        return NotificationPayloadRequest.builder()
                .body(body)
                .title(type.getTitle())
                .build();
    }

    /**
     * 신청폼 반려에 대해 알림내용을 만들어주는 메서드입니다.
     */
    public NotificationPayloadRequest rejectNotification(ApplicationFormRejectedType applicationFormRejectedType, Member agent, String memo) {
        if (applicationFormRejectedType == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 매핑 정보에 따라 NotificationType 구하기
        ApplicationFormRejectNotificationType type = ApplicationFormRejectNotificationType.from(applicationFormRejectedType);
        if (type == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 알림 메시지 생성
        Map<String, String> placeHolder = new HashMap<>();
        String nickname = agent.getNickname();
        placeHolder.put(PLACE_HOLDER_NICK_NAME_KEY, nickname);
        String title = type.formatTitle(placeHolder);

        // '기타' 거절 사유일 때 메모 messageBody에 세팅
        if (!memo.equals("none")) {
            Map<String, String> placeHolderForOtherMemo = new HashMap<>();
            placeHolderForOtherMemo.put(PLACE_HOLDER_REJECT_OTHER_MEMO, memo);
            String body = type.formatMessage(placeHolderForOtherMemo);

            return NotificationPayloadRequest.builder()
                    .body(body)
                    .title(title)
                    .build();
        }

        return NotificationPayloadRequest.builder()
                .body(type.getMessageBody())
                .title(title)
                .build();
    }
}