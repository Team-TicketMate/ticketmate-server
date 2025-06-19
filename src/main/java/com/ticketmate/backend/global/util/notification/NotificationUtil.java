package com.ticketmate.backend.global.util.notification;

import static com.ticketmate.backend.global.constant.NotificationConstants.PLACE_HOLDER_NICK_NAME_KEY;
import static com.ticketmate.backend.global.constant.NotificationConstants.PLACE_HOLDER_REJECT_OTHER_MEMO_KEY;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.domain.constant.ApplicationFormApproveNotification;
import com.ticketmate.backend.domain.notification.domain.constant.ApplicationFormRejectNotificationType;
import com.ticketmate.backend.domain.notification.domain.constant.PortfolioNotificationType;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayloadRequest;
import com.ticketmate.backend.domain.notification.repository.FcmTokenRepository;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationUtil {

  private final FcmTokenRepository fcmTokenRepository;

  /**
   * 알림을 발송할 대상의 FCM 토큰이 있는지 검증해주는 메서드입니다.
   */
  public boolean existsFcmToken(UUID memberId) {
    log.debug("토큰 확인 로직");
    boolean tokenExist = fcmTokenRepository.existsByMemberId(memberId);
    log.debug("토큰 존재 여부 : {}", tokenExist);

    return tokenExist;
  }

  /**
   * 알림을 발송할 대상의 FCM 토큰이 있는지 검증해주는 메서드입니다.
   */
  public boolean existsFcmToken(UUID memberId) {
    log.debug("토큰 확인 로직");
    boolean tokenExist = fcmTokenRepository.existsByMemberId(memberId);
    log.debug("토큰 존재 여부 : {}", tokenExist);

    return tokenExist;
  }

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
      placeHolderForOtherMemo.put(PLACE_HOLDER_REJECT_OTHER_MEMO_KEY, memo);
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

  /**
   * 신청폼 수락에 대해 알림내용을 만들어주는 메서드입니다.
   */
  public NotificationPayloadRequest approveNotification(Member agent) {
    ApplicationFormApproveNotification type = ApplicationFormApproveNotification.APPROVE;

    // 알림 메시지 생성
    Map<String, String> placeHolder = new HashMap<>();
    String nickname = agent.getNickname();
    placeHolder.put(PLACE_HOLDER_NICK_NAME_KEY, nickname);
    String body = type.formatMessage(placeHolder);

    return NotificationPayloadRequest.builder()
        .body(body)
        .title(type.getTitle())
        .build();
  }
}