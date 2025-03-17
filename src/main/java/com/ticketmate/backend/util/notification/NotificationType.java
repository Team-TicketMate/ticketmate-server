package com.ticketmate.backend.util.notification;

import com.ticketmate.backend.object.constants.PortfolioType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum NotificationType {
    /*
   ======================================포트폴리오======================================
    */
    REVIEWING("포트폴리오 검토중","관리자가 {nickname} 님의 포트폴리오를 검토중입니다."),

    REVIEW_COMPLETED("포트폴리오 승인 완료","{nickname} 님의 포트폴리오가 관리자에 의해 승인처리 되었습니다."),

    COMPANION("포트폴리오 반려","{nickname} 님의 포트폴리오가 관리자에 의해 반려처리 되었습니다.");

    /*
   ======================================추후 필요한 알림 템플릿 작성======================================
    */


    private final String title;
    private final String messageBody;

    /**
     * 메시지 템플릿에 {nickname} 변수를 치환해주는 메서드입니다.
     */
    public String formatMessage(String nickname) {
        if (nickname == null) {
            nickname = "알 수 없음";
        }
        return messageBody.replace("{nickname}", nickname);
    }

    /**
     * (1) PortfolioType → NotificationType 매핑용 Map
     */
    private static final Map<PortfolioType, NotificationType> MAPPING = new HashMap<>();
    static {
        MAPPING.put(PortfolioType.REVIEWING, REVIEWING);
        MAPPING.put(PortfolioType.REVIEW_COMPLETED, REVIEW_COMPLETED);
        MAPPING.put(PortfolioType.COMPANION, COMPANION);
    }

    /**
     * (2) 정적 메서드로 매핑된 NotificationType 얻기
     */
    public static NotificationType from(PortfolioType portfolioType) {
        return MAPPING.get(portfolioType);
    }
}
