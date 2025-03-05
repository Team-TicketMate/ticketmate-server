package com.ticketmate.backend.object.redis;

import com.ticketmate.backend.object.constants.MemberPlatform;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@RedisHash(value = "fcmToken", timeToLive = 2592000)  // 30일 지나면 파기
@Setter
public class FcmToken {
    @Id
    private String tokenId; // 회원 PK + "-" + MemberPlatform 형태
    private String fcmToken;  // FCM 토큰
    private UUID memberId;  // 사용자의 PK
    private MemberPlatform memberPlatform;  // 토큰을 받은 사용자의 기기종류

    @Builder
    public FcmToken(String fcmToken, UUID memberId, MemberPlatform memberPlatform) {
        this.tokenId = memberId + "-" + memberPlatform;
        this.fcmToken = fcmToken;
        this.memberId = memberId;
        this.memberPlatform = memberPlatform;
    }
}
