package com.ticketmate.backend.api.application.controller.chat;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.chat.application.dto.request.PictureMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;

public interface ChatMessageControllerDocs {

  @ApiChangeLogs({
          @ApiChangeLog(
                  date = "2025-07-18",
                  author = "mr6208",
                  description = "사진 전송 API 개발",
                  issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/194"
          )
  })
  @Operation(
          summary = "1:1 채팅 사진 메시지 전송",
          description = """
      지정한 채팅방에 여러 장 혹은 단일 이미지를 한 번에 전송합니다.
      
      해당 API는 인증이 필요합니다.

      ### 요청 형식
      - **HTTP Method** : `POST`
      - **Content-Type** : `multipart/form-data`
      - **Path Variable**
        - `chat-room-id` (String, required) : 채팅방 PK
      - **Form-Data (ModelAttribute)**
        - `chatMessagePictureList` (List&lt;MultipartFile&gt;, required)  
          전송할 이미지 파일 목록(최대 10장)

      ### 동작
      1. 모든 파일을 S3(UploadType.CHAT)에 업로드(오류 시 롤백)  
      2. DB(MongoDB)에 `ChatMessage`(type=PICTURE) 저장  
      3. 발송자·수신자에게 RabbitMQ-WebSocket 브로드캐스트  
      4. HTTP 응답 본문 없음(상태 코드로만 성공/실패 판별)

      ### 사용-시 유의사항
      1. **반드시 `multipart/form-data`**로 전송하고 파라미터 이름은 `chatMessagePictureList`여야 합니다.
      2. 성공 후 클라이언트는 `chat.room.{chatRoomId}.user.{memberId}` 채널을 구독해 UI를 갱신해야 합니다.
      3. 같은 요청 내에서 업로드 가능한 최대 장수는 **10장**입니다.
      4. 실패 시 반환되는 `ErrorResponse.errorCode`를 확인해 원인을 파악하세요.

      ---
      ### 브로드캐스트 예시
      #### ① 채팅 메시지 (`ChatMessageResponse`)
      ```json
      {
        "chatRoomId": "6851527aa7c9fa3807ca962a",
        "messageId": "c1b99d88-a9a4-4c4c-80e8-6c5212e53b0d",
        "senderId": "17530e2c-3280-42d8-b418-8c6e27e7e76b",
        "senderNickname": "정우혁",
        "message": null,
        "sendDate": "2025-07-18T17:45:12",
        "profileUrl": "https://…/avatar.png",
        "mine": false,
        "chatMessageType": "PICTURE",
        "pictureMessageList": [
          "https://…/chat/202507/pic1.jpg",
          "https://…/chat/202507/pic2.jpg"
        ],
        "isRead": false
      }
      ```

      #### ② 읽지 않은 메시지 카운터 업데이트
      ```json
      {
        "chatRoomId": "6851527aa7c9fa3807ca962a",
        "unReadMessageCount": 5,
        "lastMessage": "사진을 보냈습니다.",
        "lastMessageType": "PICTURE",
        "sendDate": "2025-07-18T17:45:12",
        "lastMessageId": "c1b99d88-a9a4-4c4c-80e8-6c5212e53b0d"
      }
      ```
      """
  )
  @Parameters({
          @Parameter(
                  name = "chat-room-id",
                  description = "채팅방 PK(UUID 24자 문자열)",
                  required = true,
                  in = ParameterIn.PATH,
                  example = "6851527aa7c9fa3807ca962a"
          )
  })
  ResponseEntity<Void> sendPictureMessage(String chatRoomId, PictureMessageRequest request, CustomOAuth2User customOAuth2User);
}
