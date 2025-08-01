package com.ticketmate.backend.notification.application.type;

import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.application.util.NotificationUtil;
import com.ticketmate.backend.notification.core.constant.NotificationConstants;
import com.ticketmate.backend.notification.core.type.DomainNotificationType;
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

  private final String title;
  private final String messageBody;

  /**
   * 신청서 거절 알림 payload 생성
   *
   * @param nickname  대리인 닉네임
   * @param otherMemo "기타" 거절 시 메모
   * @return NotificationPayload
   */
  public NotificationPayload toPayload(String nickname, String otherMemo) {
    Map<String, String> placeholder = new HashMap<>();
    placeholder.put(NotificationConstants.PLACEHOLDER_NICKNAME_KEY, nickname);

    if (this == OTHER) {
      placeholder.put(NotificationConstants.PLACEHOLDER_REJECT_OTHER_MEMO_KEY, otherMemo);
    }

    return NotificationUtil.createNotification(this, placeholder);
  }
}