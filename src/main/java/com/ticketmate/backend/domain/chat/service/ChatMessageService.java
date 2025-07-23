package com.ticketmate.backend.domain.chat.service;

import com.ticketmate.backend.domain.chat.domain.constant.ChatMessageType;
import com.ticketmate.backend.domain.chat.domain.dto.request.ChatMessageRequest;
import com.ticketmate.backend.domain.chat.domain.dto.request.PictureMessageRequest;
import com.ticketmate.backend.domain.chat.domain.dto.request.ReadAckRequest;
import com.ticketmate.backend.domain.chat.domain.dto.request.TextMessageRequest;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatMessageResponse;
import com.ticketmate.backend.domain.chat.domain.dto.response.ReadAckResponse;
import com.ticketmate.backend.domain.chat.domain.entity.ChatMessage;
import com.ticketmate.backend.domain.chat.domain.entity.ChatRoom;
import com.ticketmate.backend.domain.chat.domain.entity.LastReadMessage;
import com.ticketmate.backend.domain.chat.repository.ChatMessageRepository;
import com.ticketmate.backend.domain.chat.repository.ChatRoomRepository;
import com.ticketmate.backend.domain.chat.repository.LastReadMessageRepository;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.global.constant.ChatConstants;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.file.constant.UploadType;
import com.ticketmate.backend.global.file.service.S3Service;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.ticketmate.backend.global.constant.ChatConstants.*;
import static com.ticketmate.backend.global.constant.RabbitMqConstants.CHAT_EXCHANGE_NAME;
import static com.ticketmate.backend.global.constant.RabbitMqConstants.UN_READ_ROUTING_KEY;
import static com.ticketmate.backend.global.exception.ErrorCode.CHAT_ROOM_NOT_FOUND;
import static com.ticketmate.backend.global.exception.ErrorCode.MESSAGE_NOT_FOUND;

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
  private final S3Service s3Service;

  /**
   * 채팅 메시지를 보내는 메서드입니다.
   */
  @Transactional
  public void sendMessage(String chatRoomId, ChatMessageRequest request, Member sender) {

    // 메시지를 보낼 채팅방 조회
    ChatRoom chatRoom = findChatRoom(chatRoomId);

    ChatMessage chatMessage = handleNewChatMessage(sender, request, chatRoom);

    // 채팅방 두 참가자(자신 및 상대)에게 각각 1회씩 전송
    Stream.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId())
            .forEach(currentMemberId -> {
              ChatMessageResponse chatMessageResponse = mapper.toChatMessageResponse(chatMessage, currentMemberId);

              rabbitTemplate.convertAndSend(
                      CHAT_EXCHANGE_NAME,
                      "chat.room." + chatRoomId + ".user." + currentMemberId, chatMessageResponse);
            });
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

                ChatMessage lastMessage = chatMessageRepository.findById(ack.getLastReadMessageId())
                        .orElseThrow(() -> new CustomException(MESSAGE_NOT_FOUND));

                ReadAckResponse readAckResponse = ReadAckResponse.builder()
                        .chatRoomId(chatRoomId)
                        .readerId(reader.getMemberId())
                        .senderId(lastMessage.getSenderId())
                        .lastReadMessageId(ack.getLastReadMessageId())
                        .readDate(lastMessage.getSendDate())
                        .build();

                Stream.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId()).
                        filter(currentMemberId -> !currentMemberId.equals(reader.getMemberId()))
                        .forEach(currentMemberId -> rabbitTemplate.convertAndSend(
                                CHAT_EXCHANGE_NAME,
                                "chat.room." + chatRoomId + ".user." + currentMemberId, readAckResponse));
              }
            });
  }

  private LastReadMessage findLastReadMessage(ReadAckRequest request, String chatRoomId, Member reader) {

    // Redis포인터 기본 키값 추출
    String redisKey = LAST_READ_MESSAGE_POINTER_KEY.formatted(chatRoomId, reader.getMemberId());

    // 추출한 키값 조회 후 없으면 새로 생성 후 포인터 갱신 진행
    return lastReadMessageRepository.findById(redisKey)
            .orElseGet(() ->
                    LastReadMessage.builder()
                            .lastReadMessage(redisKey)
                            .chatRoomId(chatRoomId)
                            .memberId(reader.getMemberId())
                            .lastMessageId(request.getLastReadMessageId())
                            .readDate(request.getReadDate())
                            .build()
            );
  }

  private String formattingSendDate(LocalDateTime dateTime) {
    return dateTime.truncatedTo(ChronoUnit.SECONDS)
            .format(ISO_SEC);
  }

  /**
   * 채팅 메시지 저장 + Redis를 이용해 읽지않은 메시지 count, 및 브로드캐스팅을 관리하는 메서드
   */
  private ChatMessage handleNewChatMessage(Member sender, ChatMessageRequest request, ChatRoom chatRoom) {

    ChatMessage message = saveChatMessage(sender, request, chatRoom);

    List<UUID> chatRoomMemberIdList = List.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId());

    // Redis 갱신
    for (UUID chatRoomMemberId : chatRoomMemberIdList) {
      if (chatRoomMemberId.equals(sender.getMemberId())) {
        continue;   // 발송자 제외
      }

      String key = UN_READ_MESSAGE_COUNTER_KEY.formatted(chatRoom.getChatRoomId(), chatRoomMemberId);
      Long count = redisTemplate.opsForValue().increment(key);
      redisTemplate.expire(key, TTL);

      String formattedSendDate = formattingSendDate(message.getSendDate());

      rabbitTemplate.convertAndSend(
              "",
              UN_READ_ROUTING_KEY + chatRoomMemberId,
              Map.of(
                      "chatRoomId", chatRoom.getChatRoomId(),
                      "unReadMessageCount", count,
                      "lastMessage", request.toPreview(),
                      "lastMessageType", request.getType(),
                      "sendDate", formattedSendDate,
                      "lastMessageId", message.getChatMessageId()
              ));
    }
    return message;
  }

  /**
   * 채팅 메시지 저장 메서드 (중복로직 떄문에 추출)
   */
  private ChatMessage saveChatMessage(Member sender, ChatMessageRequest request, ChatRoom chatRoom) {
    /**
     * 현재 보낸 메시지가 'TEXT' 인지 'PICTURE' 인지에 대한 분기처리
     */
    ChatMessage chatMessage = switch (request.getType()) {
      case TEXT -> saveTextMessage(((TextMessageRequest) request).getMessage(), sender, chatRoom, ChatMessageType.TEXT);

      case PICTURE -> savePictureMessage((PictureMessageRequest) request, sender, chatRoom);

      default -> throw new CustomException(ErrorCode.CHAT_MESSAGE_ERROR);
    };

    updateLastMessageInfo(chatRoom, chatMessage, request.toPreview());

    // Mongo 더티체크 X 즉, 데이터 변경시 직접 저장을 해줘야 함.
    chatRoomRepository.save(chatRoom);

    log.debug("메시지가 저장완료 Sender : {}", chatMessage.getSenderId());
    return chatMessage;
  }

  /**
   * 요청된 채팅방을 조회하는 메서드
   */
  private ChatRoom findChatRoom(String chatRoomId) {
    return chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));
  }

  /**
   * 텍스트 채팅 메시지 저장 로직
   */
  private ChatMessage saveTextMessage(String message, Member sender, ChatRoom chatRoom, ChatMessageType chatMessageType) {
    ChatMessage chatMessage = ChatMessage.builder()
            .chatRoomId(chatRoom.getChatRoomId())
            .senderId(sender.getMemberId())
            .senderNickName(sender.getNickname())
            .senderEmail(sender.getUsername())
            .senderProfileUrl(sender.getProfileUrl())
            .message(message)
            .chatMessageType(chatMessageType)
            .isRead(false)
            .sendDate(LocalDateTime.now())
            .pictureMessageList(Collections.emptyList())
            .build();

    chatMessageRepository.save(chatMessage);
    return chatMessage;
  }

  /**
   * 이미지(사진) 채팅 메시지 저장 로직
   */
  private ChatMessage savePictureMessage(PictureMessageRequest request, Member sender, ChatRoom chatRoom) {

    // 받아온 파일 리스트 추출
    List<MultipartFile> pictureList = request.getChatMessagePictureList();

    // 사진 리스트 검증
    if (CommonUtil.nullOrEmpty(pictureList)) {
      throw new CustomException(ErrorCode.CHAT_PICTURE_EMPTY);
    }
    if (pictureList.size() > ChatConstants.CHAT_PICTURE_MAX_SIZE) {
      throw new CustomException(ErrorCode.CHAT_PICTURE_SIZE_EXCEED);
    }

    // Url을 저장할 리스트 세팅
    List<String> pictureUrlList = new ArrayList<>(pictureList.size());

    // 파일 다중 저장
    try {
      for (MultipartFile picture : pictureList) {
        if (picture == null || picture.isEmpty()) {
          throw new CustomException(ErrorCode.CHAT_PICTURE_EMPTY);
        }
        String pictureUrl = s3Service.uploadFile(picture, UploadType.CHAT);
        pictureUrlList.add(pictureUrl);
      }
    } catch (Exception e) {
      log.error("채팅 이미지 업로드 중 오류: {}, 업로드된 {}개 파일 롤백.", e.getMessage(), pictureUrlList.size(), e);
      pictureUrlList.forEach(s3Service::safeDeleteFile);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }

    ChatMessage chatMessage = ChatMessage.builder()
            .chatRoomId(chatRoom.getChatRoomId())
            .senderId(sender.getMemberId())
            .senderNickName(sender.getNickname())
            .senderEmail(sender.getUsername())
            .senderProfileUrl(sender.getProfileUrl())
            .message(null)
            .chatMessageType(ChatMessageType.PICTURE)
            .isRead(false)
            .sendDate(LocalDateTime.now())
            .pictureMessageList(pictureUrlList)
            .build();

    return chatMessageRepository.save(chatMessage);
  }

  /**
   * 마지막 메시지 갱신을 위한 메서드
   */
  private void updateLastMessageInfo(ChatRoom chatRoom, ChatMessage chatMessage, String preViewMessage) {
    chatRoom.updateLastMessageTime(chatMessage.getSendDate());

    // 사진 -> "사진을 보냈습니다." / 텍스트 -> 채팅 내용 그대로
    chatRoom.updateLastMessage(preViewMessage);
    log.debug("세팅된 마지막 메시지: {}", chatRoom.getLastMessage());

    chatRoom.updateLastMessageId(chatMessage.getChatMessageId());

    chatRoom.updateLastMessageType(chatMessage.getChatMessageType());
  }
}