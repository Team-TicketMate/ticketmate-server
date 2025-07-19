package com.ticketmate.backend.domain.notification.domain.constant;

import static com.ticketmate.backend.global.constant.NotificationConstants.PLACEHOLDER_NICKNAME_KEY;

import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayload;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
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

  REJECTED("포트폴리오 반려", "{nickname} 님의 포트폴리오가 관리자에 의해 반려처리 되었습니다.");


  /**
   * PortfolioType(Key) → PortfolioNotificationType(Value) 매핑용 Map
   */
  private static final Map<PortfolioType, PortfolioNotificationType> MAP = new HashMap<>();

  static {
    MAP.put(PortfolioType.REVIEWING, REVIEWING);
    MAP.put(PortfolioType.APPROVED, REVIEW_COMPLETED);
    MAP.put(PortfolioType.REJECTED, REJECTED);
  }

  private final String title;
  private final String messageBody;

  /**
   * PortfolioType과 매핑된 PortfolioNotificationType 반환
   */
  public static PortfolioNotificationType from(PortfolioType portfolioType) {
    return MAP.get(portfolioType);
  }

  /**
   * 포트폴리오 알림 payload 생성
   */
  public NotificationPayload toPayload(Member member) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(PLACEHOLDER_NICKNAME_KEY, member.getNickname());
    return NotificationUtil.createNotification(this, placeholder);
  }
}