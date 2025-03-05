package com.ticketmate.backend.object.dto.fcm.response;


import com.ticketmate.backend.object.constants.MemberPlatform;
import lombok.*;

import java.util.UUID;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FcmTokenSaveResponse {
    private String tokenId;  // 서버에 저장된 fcm 엔티티의 PK값
    private String fcmToken;  // 반환할 fcm 토큰값
    private UUID memberId;  // 사용자 PK
    private MemberPlatform memberPlatform;  // 로그인한 사용자 기기종류
}
