package com.ticketmate.backend.api.application.controller.notification;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.notification.application.dto.request.FcmTokenSaveRequest;
import com.ticketmate.backend.notification.application.dto.response.FcmTokenSaveResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface FcmControllerDocs {

  @Operation(
      summary = "클라이언트로부터 발급한 FCM 토큰 및 사용자의 기기정보를 받아 저장하는 API 입니다.",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **fcmToken** (String): FCM 토큰값 [필수]
          - **deviceType** (String): 사용자 기기값 [필수]
          
          ### MemberPlatform
          
              ANDROID("안드로이드")
              IOS("애플")
              WEB("웹")
              OTHER("기타")
          
          ### 반환값
             - tokenId [String] (서버에 저장된 fcm 엔티티의 PK값)
             - fcmToken [String] (반환할 fcm 토큰값)
             - memberId [String] (사용자 PK)
             - deviceType [String] (로그인한 사용자 기기종류)
          
          
          ### 유의사항
          - FCM 토큰과 ios, 안드로이드, 웹페이지로부터 각각의 사용자 기기정보를 받습니다.
          - 사용자당 최소 1개 이상의 FCM 토큰이 있을 수 있습니다 (사용자의 다중 기기접속시 1:N)
          """
  )
  ResponseEntity<FcmTokenSaveResponse> saveFcmToken(
      CustomOAuth2User customOAuth2User,
      FcmTokenSaveRequest request);
}
