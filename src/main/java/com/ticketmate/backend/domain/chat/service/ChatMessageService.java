package com.ticketmate.backend.domain.chat.service;

import com.ticketmate.backend.domain.chat.domain.dto.request.ChatMessageRequest;
import com.ticketmate.backend.domain.chat.domain.dto.request.ReadAckRequest;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatMessageResponse;
import com.ticketmate.backend.domain.chat.domain.dto.response.ReadAckResponse;
import com.ticketmate.backend.domain.chat.domain.entity.ChatMessage;
import com.ticketmate.backend.domain.chat.domain.entity.ChatRoom;
import com.ticketmate.backend.domain.chat.domain.entity.LastReadMessage;
import com.ticketmate.backend.domain.chat.repository.ChatMessageRepository;
import com.ticketmate.backend.domain.chat.repository.ChatRoomRepository;
import com.ticketmate.backend.domain.chat.repository.LastReadMessageRepository;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.mapper.EntityMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.ticketmate.backend.global.constant.ChatConstants.*;
import static com.ticketmate.backend.global.exception.ErrorCode.CHAT_ROOM_NOT_FOUND;
import static com.ticketmate.backend.global.exception.ErrorCode.MESSAGE_NOT_FOUND;
import static com.ticketmate.backend.global.util.rabbit.RabbitMq.CHAT_EXCHANGE_NAME;
import static com.ticketmate.backend.global.util.rabbit.RabbitMq.UN_READ_ROUTING_KEY;

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

  /**
   * 채팅 메시지를 보내는 메서드입니다.
   */
  @Transactional
  public void sendMessage(String chatRoomId, ChatMessageRequest request, Member sender) {

    // 메시지를 보낼 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

    // 메시지 발송
    ChatMessage chatMessage = saveMessage(sender, request, chatRoom);

    // 채팅방 두 참가자(자신 및 상대)에게 각각 1회씩 전송
    Stream.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId())
            .forEach(currentMemberId -> {
              ChatMessageResponse chatMessageResponse = mapper.toChatMessageResponse(chatMessage, currentMemberId);

              rabbitTemplate.convertAndSend(
                      CHAT_EXCHANGE_NAME,
                      "chat.room." + chatRoomId + ".user." + currentMemberId, chatMessageResponse);
            });
  }

  private ChatMessage saveMessage(Member sender, ChatMessageRequest request, ChatRoom chatRoom) {

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
      if (target.equals(sender.getMemberId())) {
        continue;   // 발송자 제외
      }

      String key = UN_READ_MESSAGE_COUNTER_KEY.formatted(chatRoom.getChatRoomId(), target);
      Long count = redisTemplate.opsForValue().increment(key);
      redisTemplate.expire(key, TTL);

      String formattedSendDate = formattingSendDate(message.getSendDate());

      rabbitTemplate.convertAndSend(
              "",
              UN_READ_ROUTING_KEY + target,
              Map.of(
                      "chatRoomId", chatRoom.getChatRoomId(),
                      "unReadMessageCount", count,
                      "lastMessage", request.getMessage(),
                      "sendDate", formattedSendDate,
                      "lastMessageId", message.getChatMessageId()
              ));
    }
    return message;
  }

  /**
   * 사용자가 채팅방에서 마지막으로 본 메시지(또는 그 이후)를 보고
   * Read ACK 를 보냈을 때 호출된다.
   */
  @Transactional
  public void acknowledgeRead(ReadAckRequest ack, Member reader, String chatRoomId) {

    log.debug("acknowledgeRead 메서드 동작");

    LastReadMessage lastReadMessagePointer = findLastReadMessage(ack, chatRoomId, reader);

    lastReadMessagePointer.updatePointer(ack.getLastReadMessageId(), ack.getReadDate());
    lastReadMessageRepository.save(lastReadMessagePointer); // TTL(30일)

    // Redis 카운터 제거
    String unReadRedisKey = UN_READ_MESSAGE_COUNTER_KEY.formatted(chatRoomId, reader.getMemberId());
    redisTemplate.delete(unReadRedisKey);

    // 채팅방 리스트에 즉시 갱신하기 위한 코드
    rabbitTemplate.convertAndSend(
            "",
            UN_READ_ROUTING_KEY + reader.getMemberId(),
            Map.of("chatRoomId", chatRoomId,
                    "unReadMessageCount", 0,
                    "lastMessageId", ack.getLastReadMessageId())
    );

    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

    long updatedMessage = chatMessageRepository.markReadUpTo(chatRoomId, reader.getMemberId());
    log.debug("'읽음' 처리된 메시지 개수  = {}", updatedMessage);

    // 읽음 이벤트 브로드캐스트 (트랜젝션 커밋 직후)
    TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
              @Override
              public void afterCommit() {

                ChatMessage last = chatMessageRepository.findById(ack.getLastReadMessageId())
                        .orElseThrow(() -> new CustomException(MESSAGE_NOT_FOUND));

                ReadAckResponse readAckResponse = ReadAckResponse.builder()
                        .chatRoomId(chatRoomId)
                        .readerId(reader.getMemberId())
                        .senderId(last.getSenderId())
                        .lastReadMessageId(ack.getLastReadMessageId())
                        .readDate(last.getSendDate())
                        .build();

                Stream.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId()).
                        filter(currentMemberId -> !currentMemberId.equals(reader.getMemberId()))
                        .forEach(currentMemberId -> rabbitTemplate.convertAndSend(
                                CHAT_EXCHANGE_NAME,
                                "chat.room." + chatRoomId + ".user." + currentMemberId, readAckResponse));
              }
            });
  }

  private LastReadMessage findLastReadMessage(ReadAckRequest ack, String chatRoomId, Member reader) {

    // Redis포인터 기본 키값 추출
    String redisKey = LAST_READ_MESSAGE_POINTER_KEY.formatted(chatRoomId, reader.getMemberId());

    // 추출한 키값 조회 후 없으면 새로 생성 후 포인터 갱신 진행
    return lastReadMessageRepository.findById(redisKey)
            .orElseGet(() ->
                    LastReadMessage.builder()
                            .lastReadMessage(redisKey)
                            .chatRoomId(chatRoomId)
                            .memberId(reader.getMemberId())
                            .lastMessageId(ack.getLastReadMessageId())
                            .readDate(ack.getReadDate())
                            .build()
            );
  }

  private String formattingSendDate(LocalDateTime dateTime) {
    return dateTime.truncatedTo(ChronoUnit.SECONDS)
            .format(ISO_SEC);
  }
}