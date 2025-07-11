package com.ticketmate.backend.domain.notification.domain.constant;

import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PortfolioNotificationType implements DomainNotificationType {
  /*
 ======================================포트폴리오======================================
  */
  REVIEWING("포트폴리오 검토중", "관리자가 {nickname} 님의 포트폴리오를 검토중입니다."),

  REVIEW_COMPLETED("포트폴리오 승인 완료", "{nickname} 님의 포트폴리오가 관리자에 의해 승인처리 되었습니다."),

  COMPANION("포트폴리오 반려", "{nickname} 님의 포트폴리오가 관리자에 의해 반려처리 되었습니다.");


  /**
   * (1) PortfolioType → PortfolioNotificationType 매핑용 Map
   */
  private static final Map<PortfolioType, PortfolioNotificationType> MAPPING = new HashMap<>();

  static {
    MAPPING.put(PortfolioType.IN_REVIEW, REVIEWING);
    MAPPING.put(PortfolioType.ACCEPTED, REVIEW_COMPLETED);
    MAPPING.put(PortfolioType.REJECTED, COMPANION);
  }

  private final String title;
  private final String messageBody;

  /**
   * (2) 정적 메서드로 매핑된 PortfolioNotificationType 얻기
   */
  public static PortfolioNotificationType from(PortfolioType portfolioType) {
    return MAPPING.get(portfolioType);
  }
}