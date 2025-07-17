package com.ticketmate.backend.domain.notification.domain.constant;

import static com.ticketmate.backend.global.constant.NotificationConstants.PLACEHOLDER_NICKNAME_KEY;
import static com.ticketmate.backend.global.constant.NotificationConstants.PLACEHOLDER_REJECT_OTHER_MEMO_KEY;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayload;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
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
   * ApplicationRejectedType(Key) → ApplicationFormRejectNotificationType(Value) 매핑용 Map
   */
  private static final Map<ApplicationFormRejectedType, ApplicationFormRejectNotificationType> MAP = new HashMap<>();

  static {
    MAP.put(ApplicationFormRejectedType.FEE_NOT_MATCHING_MARKET_PRICE, FEE_NOT_MATCHING_MARKET_PRICE);
    MAP.put(ApplicationFormRejectedType.RESERVATION_CLOSED, RESERVATION_CLOSED);
    MAP.put(ApplicationFormRejectedType.SCHEDULE_UNAVAILABLE, SCHEDULE_UNAVAILABLE);
    MAP.put(ApplicationFormRejectedType.OTHER, OTHER);
  }

  private final String title;
  private final String messageBody;

  /**
   * ApplicationFormRejectedType과 매핑된 ApplicationFormRejectNotificationType 반환
   */
  public static ApplicationFormRejectNotificationType from(ApplicationFormRejectedType rejectedType) {
    return MAP.get(rejectedType);
  }

  /**
   * 신청서 거절 알림 payload 생성
   *
   * @param agent     대리인
   * @param otherMemo "기타" 거절 시 메모
   * @return NotificationPayload
   */
  public NotificationPayload toPayload(Member agent, String otherMemo) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(PLACEHOLDER_NICKNAME_KEY, agent.getNickname());

    if (this == OTHER) {
      placeholder.put(PLACEHOLDER_REJECT_OTHER_MEMO_KEY, otherMemo);
    }

    return NotificationUtil.createNotification(this, placeholder);
  }
}