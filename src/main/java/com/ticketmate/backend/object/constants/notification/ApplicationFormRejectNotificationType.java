package com.ticketmate.backend.object.constants.notification;

import com.ticketmate.backend.object.constants.ApplicationFormRejectedType;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationFormRejectNotificationType implements DomainNotificationType {
  /*
======================================신청폼 반려======================================
 */
  FEE_NOT_MATCHING_MARKET_PRICE("대리인 {nickname} 님께서 의뢰인님의 신청폼을 반려했습니다.", "사유 : 수고비가 시세에 맞지 않음"),

  RESERVATION_CLOSED("대리인 {nickname} 님께서 의뢰인님의 신청폼을 반려했습니다.", "사유 : 예약 마감"),

  SCHEDULE_UNAVAILABLE("대리인 {nickname} 님께서 의뢰인님의 신청폼을 반려했습니다.", "사유 : 티켓팅 일정이 안됨"),

  OTHER("대리인 {nickname} 님께서 의뢰인님의 신청폼을 반려했습니다.", "사유 : {otherReject}");

  /**
   * (1) ApplicationRejectedType → ExpressionsNotificationType 매핑용 Map
   */
  private static final Map<ApplicationFormRejectedType, ApplicationFormRejectNotificationType> MAPPING = new HashMap<>();

  static {
    MAPPING.put(ApplicationFormRejectedType.FEE_NOT_MATCHING_MARKET_PRICE, FEE_NOT_MATCHING_MARKET_PRICE);
    MAPPING.put(ApplicationFormRejectedType.RESERVATION_CLOSED, RESERVATION_CLOSED);
    MAPPING.put(ApplicationFormRejectedType.SCHEDULE_UNAVAILABLE, SCHEDULE_UNAVAILABLE);
    MAPPING.put(ApplicationFormRejectedType.OTHER, OTHER);
  }

  private final String title;
  private final String messageBody;

  /**
   * (2) 정적 메서드로 매핑된 ExpressionsNotificationType 얻기
   */
  public static ApplicationFormRejectNotificationType from(ApplicationFormRejectedType rejectedType) {
    return MAPPING.get(rejectedType);
  }
}