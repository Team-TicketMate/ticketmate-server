package com.ticketmate.backend.controller.chat;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.chat.reqeust.ChatMessageRequest;
import com.ticketmate.backend.object.dto.chat.reqeust.ReadAckRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.service.chat.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    /**
     * 실제로 메시지를 발송하는 컨트롤러입니다.
     * @param roomId (채팅방 Id)
     * @param request (메시지 DTO)
     */
    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload ChatMessageRequest request,
                            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Member member = customOAuth2User.getMember();
        chatMessageService.sendMessage(request, member, roomId);
    }

    @MessageMapping("chat.read.{roomId}")
    public void updateRead(@Payload ReadAckRequest ack, @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                           @DestinationVariable String roomId) {
        Member member = customOAuth2User.getMember();
        chatMessageService.acknowledgeRead(roomId, member, ack.getUptoMessageId(), ack.getReadAt());
    }
}
