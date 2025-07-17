package com.ticketmate.backend.domain.notification.domain.entity;

import com.ticketmate.backend.domain.notification.domain.constant.DeviceType;
import com.ticketmate.backend.global.constant.NotificationConstants;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@RedisHash(value = "fcmToken", timeToLive = NotificationConstants.FCM_TOKEN_TTL)
@Setter
public class FcmToken {

  @Id
  private String fcmTokenId; // 회원 PK + "-" + DeviceType 형태

  private String token;  // FCM 토큰
  @Indexed

  private UUID memberId;  // 사용자의 PK

  private DeviceType deviceType;  // 토큰을 받은 사용자의 기기종류

  @Builder
  public FcmToken(String token, UUID memberId, DeviceType deviceType) {
    this.fcmTokenId = memberId + "-" + deviceType;
    this.token = token;
    this.memberId = memberId;
    this.deviceType = deviceType;
  }
}
