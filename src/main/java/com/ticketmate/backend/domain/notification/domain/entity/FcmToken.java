package com.ticketmate.backend.domain.notification.domain.entity;

import com.ticketmate.backend.domain.notification.domain.constant.DeviceType;
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
@RedisHash(value = "fcmToken", timeToLive = 2592000)  // 30일 지나면 파기
@Setter
public class FcmToken {

  @Id
  private String tokenId; // 회원 PK + "-" + DeviceType 형태
  private String fcmToken;  // FCM 토큰
  @Indexed
  private UUID memberId;  // 사용자의 PK
  private DeviceType deviceType;  // 토큰을 받은 사용자의 기기종류

  @Builder
  public FcmToken(String fcmToken, UUID memberId, DeviceType deviceType) {
    this.tokenId = memberId + "-" + deviceType;
    this.fcmToken = fcmToken;
    this.memberId = memberId;
    this.deviceType = deviceType;
  }
}
