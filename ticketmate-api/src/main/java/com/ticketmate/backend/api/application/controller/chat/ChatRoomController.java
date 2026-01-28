package com.ticketmate.backend.api.application.controller.chat;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.api.application.service.EnterChatRoomService;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.chat.application.dto.request.ChatMessageFilteredRequest;
import com.ticketmate.backend.chat.application.dto.request.ChatRoomFilteredRequest;
import com.ticketmate.backend.chat.application.dto.response.ChatMessageResponse;
import com.ticketmate.backend.chat.application.dto.response.ChatRoomContextResponse;
import com.ticketmate.backend.chat.application.dto.response.ChatRoomResponse;
import com.ticketmate.backend.chat.application.service.ChatRoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room")
@Tag(
  name = "채팅방 관련 API",
  description = "1:1 채팅방 관련 API 제공"
)
public class ChatRoomController implements ChatRoomControllerDocs {

  private final ChatRoomService chatRoomService;
  private final EnterChatRoomService enterChatRoomService;

  @Override
  @GetMapping("")
  @LogMonitoring
  public ResponseEntity<Page<ChatRoomResponse>> getChatRoomList(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @ParameterObject @Valid ChatRoomFilteredRequest request) {
    return ResponseEntity.ok(chatRoomService.getChatRoomList(customOAuth2User.getMember(), request));
  }

  @Override
  @GetMapping("/{chat-room-id}/message")
  @LogMonitoring
  public ResponseEntity<Slice<ChatMessageResponse>> getChatMessages(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("chat-room-id") String chatRoomId,
    @ParameterObject @Valid ChatMessageFilteredRequest request) {
    return ResponseEntity.ok(chatRoomService.getChatMessage(customOAuth2User.getMember(), chatRoomId, request));
  }

  @Override
  @GetMapping("/{chat-room-id}/context")
  @LogMonitoring
  public ResponseEntity<ChatRoomContextResponse> enterChatRoom(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("chat-room-id") String chatRoomId) {
    return ResponseEntity.ok(enterChatRoomService.enterChatRoom(customOAuth2User.getMember(), chatRoomId));
  }

  @Override
  @GetMapping("/{chat-room-id}/application-form")
  @LogMonitoring
  public ResponseEntity<ApplicationFormInfoResponse> chatRoomApplicationFormInfo(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("chat-room-id") String chatRoomId) {
    return ResponseEntity.ok(chatRoomService.getChatRoomApplicationFormInfo(customOAuth2User.getMember(), chatRoomId));
  }

  @Override
  @PatchMapping("/{chat-room-id}/cancel-progress")
  @LogMonitoring
  public ResponseEntity<Void> cancelProgress(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("chat-room-id") String chatRoomId) {
    chatRoomService.cancelProgress(customOAuth2User.getMember(), chatRoomId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/{chat-room-id}/leave")
  @LogMonitoring
  public ResponseEntity<Void> leaveChatRoom(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("chat-room-id") String chatRoomId) {
    chatRoomService.leaveChatRoom(customOAuth2User.getMember(), chatRoomId);
    return ResponseEntity.ok().build();
  }
}
