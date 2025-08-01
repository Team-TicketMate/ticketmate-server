package com.ticketmate.backend.notification.infrastructure.service;

import com.ticketmate.backend.notification.application.dto.request.FcmTokenSaveRequest;
import com.ticketmate.backend.notification.application.dto.response.FcmTokenSaveResponse;
import com.ticketmate.backend.notification.application.mapper.NotificationMapper;
import com.ticketmate.backend.notification.infrastructure.entity.FcmToken;
import com.ticketmate.backend.notification.infrastructure.repository.FcmTokenRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;
  private final NotificationMapper notificationMapper;

  /**
   * RedisHash에 FCM 토큰 저장
   */
  @Transactional
  public FcmTokenSaveResponse saveFcmToken(FcmTokenSaveRequest request, UUID memberId) {
    // DTO -> 엔티티
    FcmToken fcmToken = FcmToken.builder()
        .token(request.getFcmToken())
        .memberId(memberId)
        .deviceType(request.getDeviceType())
        .build();

    // 같은 키(memberId-platform)에 대해 호출 시 기존 데이터가 자동으로 덮어써짐 (사용자의 기기마다 토큰값은 유일하게 설계)
    fcmTokenRepository.save(fcmToken);

    log.debug("토큰 저장 완료");
    log.debug("사용자 ID : {}", fcmToken.getMemberId());
    log.debug("사용자 기기 : {}", fcmToken.getDeviceType());

    return notificationMapper.toFcmTokenSaveResponse(fcmToken);
  }

  /**
   * Redis에 저장된 특정 사용자의 FCM 토큰을 반환합니다
   *
   * @param memberId 사용자 PK
   * @return 저장된 FCM 토큰 리스트
   */
  public List<FcmToken> findAllTokensByMemberId(UUID memberId) {
    return fcmTokenRepository.findAllByMemberId(memberId);
  }

  /**
   * Redis에 저장된 모든 FCM 토큰을 반환합니다
   */
  public List<FcmToken> findAllTokens() {
    List<FcmToken> fcmTokenList = new ArrayList<>();
    fcmTokenRepository.findAll().forEach(fcmTokenList::add);
    return fcmTokenList;
  }
}
