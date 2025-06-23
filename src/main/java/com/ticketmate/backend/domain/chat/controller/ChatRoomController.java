package com.ticketmate.backend.domain.chat.controller;

import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.chat.domain.dto.request.ChatRoomFilteredRequest;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatMessageResponse;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatRoomListResponse;
import com.ticketmate.backend.domain.chat.service.ChatRoomService;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room")
@Tag(
    name = "채팅방 관련 API",
    description = "1:1 채팅방 관련 API 제공"
)
public class ChatRoomController implements ChatRoomControllerDocs {

  private final ChatRoomService chatRoomService;

  @Override
  @GetMapping("/list")
  @LogMonitoringInvocation
  public ResponseEntity<Page<ChatRoomListResponse>> getChatRoomList(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ModelAttribute @Valid ChatRoomFilteredRequest request) {
    Member member = customOAuth2User.getMember();
    return ResponseEntity.ok(chatRoomService.getChatRoomList(member, request));
  }

  @Override
  @GetMapping("/{chat-room-id}")
  @LogMonitoringInvocation
  public ResponseEntity<List<ChatMessageResponse>> enterChatRoom(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable("chat-room-id") String chatRoomId) {
    Member member = customOAuth2User.getMember();
    return ResponseEntity.ok(chatRoomService.getChatMessage(member, chatRoomId));
  }

  @Override
  @GetMapping("/{chat-room-id}/applicationForm")
  @LogMonitoringInvocation
  public ResponseEntity<ApplicationFormFilteredResponse> chatRoomApplicationFormInfo(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                                                                     @PathVariable("chat-room-id") String chatRoomId) {

    Member member = customOAuth2User.getMember();
    return ResponseEntity.ok(chatRoomService.getChatRoomApplicationFormInfo(member, chatRoomId));
  }
}
