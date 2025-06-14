package com.ticketmate.backend.service.chat;

import com.ticketmate.backend.object.dto.chat.request.ChatMessageRequest;
import com.ticketmate.backend.object.dto.chat.request.ReadAckRequest;
import com.ticketmate.backend.object.dto.chat.response.ChatMessageResponse;
import com.ticketmate.backend.object.dto.chat.response.ReadAckResponse;
import com.ticketmate.backend.object.mongo.chat.ChatMessage;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.redis.LastReadMessage;
import com.ticketmate.backend.repository.mongo.ChatMessageRepository;
import com.ticketmate.backend.repository.mongo.ChatRoomRepository;
import com.ticketmate.backend.repository.redis.LastReadMessageRepository;
import com.ticketmate.backend.util.common.EntityMapper;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ticketmate.backend.util.rabbit.RabbitMq.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final LastReadMessageRepository lastReadMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EntityMapper mapper;
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofDays(30);
    private static final String LAST_READ_MESSAGE_POINTER_KEY = "userLastRead:%s:%s";
    private static final String UN_READ_MESSAGE_COUNTER_KEY = "unRead:%s:%s";

    /**
     * 채팅 메시지를 보내는 메서드입니다.
     */
    @Transactional
    public void sendMessage(String chatRoomId, ChatMessageRequest request, Member sender) {
        // 메시지 발송
        ChatMessageResponse messageResponse = saveMessage(sender, request, chatRoomId);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + chatRoomId, messageResponse);
    }

    private ChatMessageResponse saveMessage(Member sender, ChatMessageRequest request, String chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatMessage message = ChatMessage.builder()
                .message(request.getMessage())
                .senderProfileUrl(sender.getProfileUrl())
                .chatRoomId(chatRoom.getChatRoomId())
                .isRead(false)  // 나 이외에는 아직 안읽었다는 판정
                .senderId(sender.getMemberId())
                .senderNickName(sender.getNickname())
                .senderEmail(sender.getUsername())
                .messageType(ChatMessage.MessageType.TALK)
                .sendDate(LocalDateTime.now())
                .build();

        chatMessageRepository.save(message);

        log.debug("마지막 메시지 세팅중...");

        chatRoom.updateLastMessageTime(message.getSendDate());
        log.debug("세팅된 마지막 메시지 시간: {}", chatRoom.getLastMessageTime());

        chatRoom.updateLastMessage(message.getMessage());
        log.debug("세팅된 마지막 메시지: {}", chatRoom.getLastMessage());

        chatRoom.updateLastMessageId(message.getChatMessageId());
        log.debug("마지막 메시지Id : {}", chatRoom.getLastMessageId());

        // Mongo 더티체크 X 즉, 데이터 변경시 직접 저장을 해줘야 함.
        chatRoomRepository.save(chatRoom);

        log.debug("메시지가 저장완료 Sender : {}", message.getSenderId());
        log.debug("메시지 정보 : {}", message.getMessage());

        // Redis 갱신
        for (UUID target : List.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId())) {
            if (target.equals(sender.getMemberId())) continue;   // 발송자 제외

            String key = UN_READ_MESSAGE_COUNTER_KEY.formatted(chatRoomId, target);
            Long count = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, TTL);

//            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            rabbitTemplate.convertAndSend(
                    "",
                    UN_READ_ROUTING_KEY + target,
                    Map.of(
                            "roomId",     chatRoomId,
                            "unread",     count,
                            "lastMessage", request.getMessage(),
                            "sentAt",      message.getSendDate()
                    )
            );
        }
        return mapper.toChatMessageResponse(message);
    }

    /**
     * 사용자가 채팅방에서 마지막으로 본 메시지(또는 그 이후)를 보고
     * Read ACK 를 보냈을 때 호출된다.
     */
    @Transactional
    public void acknowledgeRead(ReadAckRequest ack, Member reader, String chatRoomId) {

        log.debug("acknowledgeRead 메서드 동작");

        // Redis포인터 기본 키값 추출
        String redisKey = LAST_READ_MESSAGE_POINTER_KEY.formatted(chatRoomId, reader.getMemberId());

        // 추출한 키값 조회 후 없으면 새로 생성 후 포인터 갱신 진행
        LastReadMessage lastReadMessagePointer = lastReadMessageRepository.findById(redisKey)
                .orElseGet(() ->
                        LastReadMessage.builder()
                                .lastReadMessage(redisKey)
                                .chatRoomId(chatRoomId)
                                .memberId(reader.getMemberId())
                                .lastMessageId(ack.getLastReadMessageId())
                                .readDate(ack.getReadDate())
                                .build()
                );

        lastReadMessagePointer.updatePointer(ack.getLastReadMessageId(), ack.getReadDate());
        lastReadMessageRepository.save(lastReadMessagePointer); // TTL(30일)

        // Redis 카운터 제거
        String unReadRedisKey = UN_READ_MESSAGE_COUNTER_KEY.formatted(chatRoomId, reader.getMemberId());
        redisTemplate.delete(unReadRedisKey);

        // 채팅방 리스트에 즉시 갱신하기 위한 코드
        rabbitTemplate.convertAndSend(
                "",
                UN_READ_ROUTING_KEY + reader.getMemberId(),
                Map.of("roomId", chatRoomId, "unread", 0)
        );

        long updatedMessage = chatMessageRepository.markReadUpTo(chatRoomId, reader.getMemberId());
        log.debug("'읽음' 처리된 메시지 개수  = {}", updatedMessage);


        // 읽음 이벤트 브로드캐스트 (트랜젝션 커밋 직후)
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ChatMessage lastMessage = chatMessageRepository.findById(ack.getLastReadMessageId()).orElseThrow(
                                () -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND)
                        );
                        rabbitTemplate.convertAndSend(
                                CHAT_EXCHANGE_NAME,
                                CHAT_ROOM_ROUTING_KEY + chatRoomId,
                                ReadAckResponse.builder()
                                        .chatRoomId(chatRoomId)
                                        .readerId(reader.getMemberId())
                                        .senderId(lastMessage.getSenderId())
                                        .lastReadMessageId(lastReadMessagePointer.getLastMessageId())
                                        .readDate(lastMessage.getSendDate())
                                        .build());
                    }
                });
    }
}