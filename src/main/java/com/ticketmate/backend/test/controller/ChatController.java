package com.ticketmate.backend.test.controller;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.test.dto.request.ChatMessageRequest;
import com.ticketmate.backend.test.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            @RequestBody ChatMessageRequest request,
                            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String nickname = customOAuth2User.getMember().getNickname();
        log.debug("엑세스토큰 만료시간: {}", customOAuth2User.getExpiresAt());
        log.debug("채팅 메시지 : 채팅방: {}, 내용={}", roomId, request.getContent());
        log.debug("현재 채팅하는 사용자 정보 : {}", nickname);

        chatService.sendMessage(request);
//        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);  // 커스텀 에러 전역처리 테스팅을 위한 예외입니다.
    }
}
