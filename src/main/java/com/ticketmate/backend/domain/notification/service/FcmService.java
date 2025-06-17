package com.ticketmate.backend.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import com.ticketmate.backend.domain.notification.domain.dto.request.FcmTokenSaveRequest;
import com.ticketmate.backend.domain.notification.domain.dto.response.FcmTokenSaveResponse;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayloadRequest;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.domain.entity.FcmToken;
import com.ticketmate.backend.domain.notification.repository.FcmTokenRepository;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

  private final FcmTokenRepository fcmTokenRepository;
  private final EntityMapper entityMapper;

  /**
   * RedisHash에 FCM 토큰이 저장되는 로직입니다.
   */
  @Transactional
  public FcmTokenSaveResponse saveFcmToken(FcmTokenSaveRequest request, Member member) {
    // DTO -> 엔티티
    FcmToken fcmToken = FcmToken.builder()
        .fcmToken(request.getFcmToken())
        .memberId(member.getMemberId())
        .memberPlatform(request.getDeviceType())
        .build();

    // 같은 키(memberId-platform)에 대해 호출 시 기존 데이터가 자동으로 덮어써짐 (사용자의 기기마다 토큰값은 유일하게 설계)
    fcmTokenRepository.save(fcmToken);

    log.debug("토큰 저장 완료");
    log.debug("사용자 ID : {}", fcmToken.getMemberId());
    log.debug("사용자 기기 : {}", fcmToken.getDeviceType());

    return entityMapper.toFcmTokenSaveResponse(fcmToken);
  }

  /**
   * @param memberId (알림을 보낼 회원의 PK)
   * @param payload  (전송할 알림 정보를 담은 DTO)
   */
  public void sendNotification(UUID memberId, NotificationPayloadRequest payload) {
    List<FcmToken> memberTokenList = fcmTokenRepository.findAllByMemberId(memberId);

    if (memberTokenList.isEmpty()) {  // 토큰이 없을 경우 검증
      throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
    }

    if (memberTokenList.size() > 1) {  // 사용자의 기기가 1개 이상일 경우 (동시접속)
      for (FcmToken fcmToken : memberTokenList) {
        sendWebNotification(fcmToken, payload);
        log.debug("다중 기기 알림전송 완료");
        log.debug("전송된 기기 : {}", fcmToken.getDeviceType());
        log.debug("알림 내용 : {}", payload);
      }
    } else {  // 사용자의 기기가 1개일경우
      FcmToken fcmToken = memberTokenList.get(0);
      sendWebNotification(fcmToken, payload);
      log.debug("단일기기 알림전송 완료");
      log.debug("전송된 기기 : {}", fcmToken.getDeviceType());
      log.debug("알림 내용 : {}", payload);

    }
  }

  /**
   * 실질적인 알림전송 로직
   */
  @Transactional
  public void sendWebNotification(FcmToken fcmToken, NotificationPayloadRequest payload) {
    try {
      // WebpushNotification 생성 (알림 제목, 본문, 아이콘 URL 등 설정)
      WebpushNotification webpushNotification = WebpushNotification.builder()
          .setTitle(payload.getTitle())
          .setBody(payload.getBody())
//                    .setIcon("") 추후 저희 어플리케이션 아이콘이 있다면 추가하면 됩니다.
          .build();

      // 웹푸시용 설정 구성 (TTL 등의 헤더 옵션 추가 가능)
      WebpushConfig webpushConfig = WebpushConfig.builder()
          .setNotification(webpushNotification)
          .putHeader("ttl", "300")
          .build();

      // 대상 토큰과 웹푸시 설정을 포함하는 Message 생성
      Message message = Message.builder()
          .setToken(fcmToken.getFcmToken())
          .setWebpushConfig(webpushConfig)
          .build();

      // FirebaseMessaging을 통해 메시지 전송
      String response = FirebaseMessaging.getInstance().send(message);
      log.debug("알림전송 완료: {}", response);

    } catch (Exception e) {
      log.debug("웹 푸시알림중 에러 발생: {}", e.getMessage());
    }
  }

//    TODO: 추후 전체사용자에 대한 알림전송이 필요하다면 비동기처리로 새로운 로직 설계
}
