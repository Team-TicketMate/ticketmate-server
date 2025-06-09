package com.ticketmate.backend.controller.chat;

import com.ticketmate.backend.controller.chat.docs.ChatRoomControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.chat.reqeust.ChatRoomFilteredRequest;
import com.ticketmate.backend.object.dto.chat.response.ChatMessageResponse;
import com.ticketmate.backend.object.dto.chat.response.ChatRoomListResponse;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.service.chat.ChatRoomService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(
        name = "채팅방 관련 API",
        description = "1:1 채팅방 관련 API 제공"
)
public class ChatRoomController implements ChatRoomControllerDocs {
    private final ChatRoomService chatRoomService;
    @Override
    @GetMapping("/chat-rooms/list")
    @LogMonitoringInvocation
    public ResponseEntity<Page<ChatRoomListResponse>> getChatRoomList(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                                                      @ModelAttribute ChatRoomFilteredRequest request) {
        Member member = customOAuth2User.getMember();
        return ResponseEntity.ok(chatRoomService.getChatRoomList(member, request));
    }

    @Override
    @GetMapping("/chat-room/{chat-room-id}")
    @LogMonitoringInvocation
    public ResponseEntity<List<ChatMessageResponse>> enterChatRoom(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                                                   @PathVariable("chat-room-id") String roomId) {
        Member member = customOAuth2User.getMember();
        return ResponseEntity.ok(chatRoomService.getChatMessage(member, roomId));
    }
}
