package com.ticketmate.backend.api.application.controller.chat;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.chat.application.dto.request.PictureMessageRequest;
import com.ticketmate.backend.chat.application.dto.request.ReadAckRequest;
import com.ticketmate.backend.chat.application.dto.request.TextMessageRequest;
import com.ticketmate.backend.chat.application.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ChatMessageController implements ChatMessageControllerDocs{

  private final ChatMessageService chatMessageService;

  /**
   * 실제로 메시지를 발송하는 컨트롤러입니다.
   *
   * @param chatRoomId (채팅방 Id)
   * @param request    (메시지 DTO)
   */
  @MessageMapping("chat.message.{roomId}")
  public void sendMessage(@DestinationVariable("roomId") String chatRoomId,
      @Payload TextMessageRequest request,
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    chatMessageService.sendMessage(chatRoomId, request, customOAuth2User.getMember());
  }

  @MessageMapping("chat.read.{roomId}")
  public void updateRead(@Payload ReadAckRequest ack, @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @DestinationVariable("roomId") String roomId) {
    chatMessageService.acknowledgeRead(ack, customOAuth2User.getMember(), roomId);
  }

  @ResponseBody
  @Override
  @PostMapping(value = "/api/chat-message/{chat-room-id}/send/pictures",
          consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> sendPictureMessage(@PathVariable(value = "chat-room-id") String chatRoomId,
                                                 @ModelAttribute @Valid PictureMessageRequest request,
                                                 @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    chatMessageService.sendMessage(chatRoomId, request, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }
}
