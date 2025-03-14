package com.ticketmate.backend.util.notification;

import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    /**
     * 포트폴리오 관련 알림내용을 만들어주는 메서드입니다.
     */
    public String portfolioNotification(PortfolioType portfolioType, Portfolio portfolio) {
        // 검토중일때 전송될 알림입니다.
        if (portfolioType.equals(PortfolioType.REVIEWING)) {
            return "관리자가 " + portfolio.getMember().getNickname() + " 님의 포트폴리오를 검토중입니다.";

            // 승인시 전송될 알림입니다.
        } else if (portfolioType.equals(PortfolioType.REVIEW_COMPLETED)) {
            return portfolio.getMember().getNickname() + " 님의 포트폴리오가 관리자에 의해 승인처리 되었습니다.";

            // 반려시 전송될 알림입니다.
        } else if (portfolioType.equals(PortfolioType.COMPANION)) {
            return portfolio.getMember().getNickname() + " 님의 포트폴리오가 관리자에 의해 반려처리 되었습니다.";
        }
        return null;
    }
}
