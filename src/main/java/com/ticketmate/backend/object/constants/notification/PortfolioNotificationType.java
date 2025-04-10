package com.ticketmate.backend.object.constants.notification;

import com.ticketmate.backend.object.constants.PortfolioType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum PortfolioNotificationType implements DomainNotificationType{
    /*
   ======================================포트폴리오======================================
    */
    REVIEWING("포트폴리오 검토중","관리자가 {nickname} 님의 포트폴리오를 검토중입니다."),

    REVIEW_COMPLETED("포트폴리오 승인 완료","{nickname} 님의 포트폴리오가 관리자에 의해 승인처리 되었습니다."),

    COMPANION("포트폴리오 반려","{nickname} 님의 포트폴리오가 관리자에 의해 반려처리 되었습니다.");


    private final String title;
    private final String messageBody;

    /**
     * (1) PortfolioType → PortfolioNotificationType 매핑용 Map
     */
    private static final Map<PortfolioType, PortfolioNotificationType> MAPPING = new HashMap<>();

    static {
        MAPPING.put(PortfolioType.REVIEWING, REVIEWING);
        MAPPING.put(PortfolioType.REVIEW_COMPLETED, REVIEW_COMPLETED);
        MAPPING.put(PortfolioType.COMPANION, COMPANION);
    }

    /**
     * (2) 정적 메서드로 매핑된 PortfolioNotificationType 얻기
     */
    public static PortfolioNotificationType from(PortfolioType portfolioType) {
        return MAPPING.get(portfolioType);
    }
}