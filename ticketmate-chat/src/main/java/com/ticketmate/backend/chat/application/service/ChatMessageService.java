package com.ticketmate.backend.chat.application.service;

import static com.ticketmate.backend.chat.infrastructure.constant.ChatConstants.ISO_SEC;
import static com.ticketmate.backend.chat.infrastructure.constant.ChatConstants.LAST_READ_MESSAGE_POINTER_KEY;
import static com.ticketmate.backend.chat.infrastructure.constant.ChatConstants.TTL;
import static com.ticketmate.backend.chat.infrastructure.constant.ChatConstants.UN_READ_MESSAGE_COUNTER_KEY;
import static com.ticketmate.backend.common.core.constant.ValidationConstants.Chat.CHAT_IMG_MAX_COUNT;
import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;

import com.ticketmate.backend.chat.application.dto.request.ChatMessageRequest;
import com.ticketmate.backend.chat.application.dto.request.FulfillmentFormMessageRequest;
import com.ticketmate.backend.chat.application.dto.request.PictureMessageRequest;
import com.ticketmate.backend.chat.application.dto.request.ReadAckRequest;
import com.ticketmate.backend.chat.application.dto.request.TextMessageRequest;
import com.ticketmate.backend.chat.application.dto.response.ChatMessageResponse;
import com.ticketmate.backend.chat.application.dto.response.ReadAckResponse;
import com.ticketmate.backend.chat.application.mapper.ChatMapper;
import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import com.ticketmate.backend.chat.infrastructure.entity.ChatMessage;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.chat.infrastructure.entity.LastReadMessage;
import com.ticketmate.backend.chat.infrastructure.repository.ChatMessageRepository;
import com.ticketmate.backend.chat.infrastructure.repository.ChatRoomRepository;
import com.ticketmate.backend.chat.infrastructure.repository.LastReadMessageRepository;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.messaging.infrastructure.properties.ChatRabbitMqProperties;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.service.StorageService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {

  private final RabbitTemplate rabbitTemplate;
  private final LastReadMessageRepository lastReadMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomService chatRoomService;
  private final ChatMessageRepository chatMessageRepository;
  private final ChatRabbitMqProperties chatRabbitMqProperties;
  private final ChatMapper chatMapper;
  private final RedisTemplate<String, String> redisTemplate;
  private final StorageService storageService;

  /**
   * 채팅 메시지를 보내는 메서드입니다.
   */
  @Transactional
  public void sendMessage(String chatRoomId, ChatMessageRequest request, Member sender) {

    // 메시지를 보낼 채팅방 조회
    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

    ChatMessage chatMessage = handleNewChatMessage(sender, request, chatRoom);

    // 채팅방 두 참가자(자신 및 상대)에게 각각 1회씩 전송
    Stream.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId())
      .forEach(currentMemberId -> {
        ChatMessageResponse chatMessageResponse = chatMapper.toChatMessageResponse(chatMessage, currentMemberId);

        rabbitTemplate.convertAndSend(
          chatRabbitMqProperties.exchangeName(),
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

    lastReadMessagePointer.updatePointer(ack.getLastReadMessageId(), TimeUtil.toInstant(ack.getReadDate()));
    lastReadMessageRepository.save(lastReadMessagePointer); // TTL(30일)

    // Redis 카운터 제거
    String unReadRedisKey = UN_READ_MESSAGE_COUNTER_KEY.formatted(chatRoomId, reader.getMemberId());
    redisTemplate.delete(unReadRedisKey);

    ChatMessage lastReadMessage = chatMessageRepository.findById(ack.getLastReadMessageId())
      .orElseThrow(() -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND));

    String preview = toPreview(lastReadMessage);
    String formattedSendDate = formattingSendDate(TimeUtil.toLocalDateTime(lastReadMessage.getSendDate()));

    rabbitTemplate.convertAndSend(
      "",
      chatRabbitMqProperties.unreadRoutingKey() + reader.getMemberId(),
      buildUnreadTopicPayload(
        chatRoomId,
        0L,
        lastReadMessage.getChatMessageId(),
        lastReadMessage.getChatMessageType(),
        preview,
        formattedSendDate
      )
    );

    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

    long updatedMessage = chatMessageRepository.markReadUpTo(chatRoomId, reader.getMemberId());
    log.debug("'읽음' 처리된 메시지 개수  = {}", updatedMessage);

    // 읽음 이벤트 브로드캐스트 (트랜젝션 커밋 직후)
    TransactionSynchronizationManager.registerSynchronization(
      new TransactionSynchronization() {
        @Override
        public void afterCommit() {

          ChatMessage lastMessage = chatMessageRepository.findById(ack.getLastReadMessageId())
            .orElseThrow(() -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND));

          ReadAckResponse readAckResponse = ReadAckResponse.builder()
            .chatRoomId(chatRoomId)
            .readerId(reader.getMemberId())
            .senderId(lastMessage.getSenderId())
            .lastReadMessageId(ack.getLastReadMessageId())
            .readDate(TimeUtil.toLocalDateTime(lastMessage.getSendDate()))
            .build();

          Stream.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId()).
            filter(currentMemberId -> !currentMemberId.equals(reader.getMemberId()))
            .forEach(currentMemberId -> rabbitTemplate.convertAndSend(
              chatRabbitMqProperties.exchangeName(),
              "chat.room." + chatRoomId + ".user." + currentMemberId, readAckResponse));
        }
      });
  }

  /**
   * 성공양식 제출 후(커밋 이후) 호출
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendFulfillmentFormMessage(String chatRoomId, UUID fulfillmentFormId, Member sender) {

    // 대상 채팅방
    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

    handleFulfillmentDecision(chatRoom,
      FulfillmentFormMessageRequest.builder()
        .fulfillmentFormId(fulfillmentFormId)
        .chatMessageType(ChatMessageType.FULFILLMENT_FORM)
        .preview(ChatMessageType.FULFILLMENT_FORM.getDescription())
        .build(),
      sender
    );
  }

  /**
   * 의뢰인이 수락했을때 발송하는 채팅
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendFulfillmentFormAcceptMessage(String chatRoomId, UUID fulfillmentFormId, Member sender) {

    // 대상 채팅방
    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

    handleFulfillmentDecision(chatRoom,
      FulfillmentFormMessageRequest.builder()
        .fulfillmentFormId(fulfillmentFormId)
        .chatMessageType(ChatMessageType.ACCEPTED_FULFILLMENT_FORM)
        .preview(ChatMessageType.ACCEPTED_FULFILLMENT_FORM.getDescription())
        .build(),
      sender
    );
  }

  /**
   * 의뢰인이 거절했을때 발송하는 채팅
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendFulfillmentFormRejectMessage(String chatRoomId, UUID fulfillmentFormId, Member sender, String rejectMemo) {
    // 대상 채팅방
    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

    handleFulfillmentDecision(chatRoom,
      FulfillmentFormMessageRequest.builder()
        .fulfillmentFormId(fulfillmentFormId)
        .chatMessageType(ChatMessageType.REJECTED_FULFILLMENT_FORM)
        .preview(ChatMessageType.REJECTED_FULFILLMENT_FORM.getDescription())
        .rejectMemo(rejectMemo) // ← 거절 사유만 세팅
        .build(),
      sender
    );
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendFulfillmentFormUpdatedMessage(String chatRoomId, UUID fulfillmentFormId, Member sender) {
    // 대상 채팅방
    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

    handleFulfillmentDecision(chatRoom,
      FulfillmentFormMessageRequest.builder()
        .fulfillmentFormId(fulfillmentFormId)
        .chatMessageType(ChatMessageType.UPDATE_FULFILLMENT_FORM)
        .preview(ChatMessageType.UPDATE_FULFILLMENT_FORM.getDescription())
        .build(),
      sender
    );
  }

  private void handleFulfillmentDecision(ChatRoom chatRoom, FulfillmentFormMessageRequest request, Member sender) {
    ChatMessage chatMessage = handleNewChatMessage(sender, request, chatRoom);

    // 참여자 라우팅
    Stream.of(chatRoom.getAgentMemberId(), chatRoom.getClientMemberId()).forEach(currentMemberId -> {
      ChatMessageResponse resp = chatMapper.toChatMessageResponse(chatMessage, currentMemberId);
      rabbitTemplate.convertAndSend(
        chatRabbitMqProperties.exchangeName(),
        "chat.room." + chatRoom.getChatRoomId() + ".user." + currentMemberId,
        resp
      );
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
          .readDate(TimeUtil.toInstant(request.getReadDate()))
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

      String formattedSendDate = formattingSendDate(TimeUtil.toLocalDateTime(message.getSendDate()));

      rabbitTemplate.convertAndSend(
        "",
        chatRabbitMqProperties.unreadRoutingKey() + chatRoomMemberId,
        buildUnreadTopicPayload(
          chatRoom.getChatRoomId(),
          count,
          message.getChatMessageId(),
          request.getType(),
          request.toPreview(),
          formattedSendDate
        )
      );
    }
    return message;
  }

  /**
   * 채팅 메시지 저장 메서드 (중복로직 떄문에 추출)
   */
  private ChatMessage saveChatMessage(Member sender, ChatMessageRequest request, ChatRoom chatRoom) {
    /**
     * 현재 보낸 메시지가 무슨 메시지 타입인지에 대한 분기처리
     */
    ChatMessage chatMessage = switch (request.getType()) {
      case TEXT -> saveTextMessage(((TextMessageRequest) request).getMessage(), sender, chatRoom, ChatMessageType.TEXT);

      case PICTURE -> savePictureMessage((PictureMessageRequest) request, sender, chatRoom);

      case FULFILLMENT_FORM, ACCEPTED_FULFILLMENT_FORM, REJECTED_FULFILLMENT_FORM, UPDATE_FULFILLMENT_FORM ->
        saveFulfillmentFormMessage((FulfillmentFormMessageRequest) request, sender, chatRoom, request.getType());
    };

    updateLastMessageInfo(chatRoom, chatMessage, request.toPreview());

    // Mongo 더티체크 X 즉, 데이터 변경시 직접 저장을 해줘야 함.
    chatRoomRepository.save(chatRoom);

    log.debug("메시지가 저장완료 Sender : {}", chatMessage.getSenderId());
    return chatMessage;
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
      .senderProfileImgStoredPath(sender.getProfileImgStoredPath())
      .message(message)
      .chatMessageType(chatMessageType)
      .isRead(false)
      .sendDate(TimeUtil.now())
      .pictureMessageStoredPathList(Collections.emptyList())
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
    if (pictureList.size() > CHAT_IMG_MAX_COUNT) {
      throw new CustomException(ErrorCode.CHAT_PICTURE_SIZE_EXCEED);
    }

    // StoredPath를 저장할 리스트 세팅
    List<String> pictureStoredPathList = new ArrayList<>(pictureList.size());

    // 파일 다중 저장
    try {
      for (MultipartFile picture : pictureList) {
        if (picture == null || picture.isEmpty()) {
          throw new CustomException(ErrorCode.CHAT_PICTURE_EMPTY);
        }
        String storedPath = storageService.uploadFile(picture, UploadType.CHAT).storedPath();
        pictureStoredPathList.add(storedPath);
      }
    } catch (Exception e) {
      log.error("채팅 이미지 업로드 중 오류: {}, 업로드된 {}개 파일 롤백.", e.getMessage(), pictureStoredPathList.size(), e);
      pictureStoredPathList.forEach(storageService::deleteFile);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }

    ChatMessage chatMessage = ChatMessage.builder()
      .chatRoomId(chatRoom.getChatRoomId())
      .senderId(sender.getMemberId())
      .senderNickName(sender.getNickname())
      .senderEmail(sender.getUsername())
      .senderProfileImgStoredPath(sender.getProfileImgStoredPath())
      .message(null)
      .chatMessageType(ChatMessageType.PICTURE)
      .isRead(false)
      .sendDate(TimeUtil.now())
      .pictureMessageStoredPathList(pictureStoredPathList)
      .build();

    return chatMessageRepository.save(chatMessage);
  }

  // 성공양식 저장 로직 (수락/거절/성공양식 제출 공통)
  private ChatMessage saveFulfillmentFormMessage(FulfillmentFormMessageRequest request, Member sender, ChatRoom chatRoom, ChatMessageType type) {

    // 타입 가드
    if (type != ChatMessageType.FULFILLMENT_FORM
        && type != ChatMessageType.ACCEPTED_FULFILLMENT_FORM
        && type != ChatMessageType.REJECTED_FULFILLMENT_FORM
        && type != ChatMessageType.UPDATE_FULFILLMENT_FORM) {
      throw new CustomException(ErrorCode.INVALID_FULFILLMENT_MESSAGE_TYPE);
    }

    // 본문 메시지(거절일 때만 메모 노출, 나머지는 UI를 위한 카드 전용이므로 null)
    final String message = (type == ChatMessageType.REJECTED_FULFILLMENT_FORM)
      ? nvl(request.getRejectMemo(), "") : null;

    ChatMessage entity = ChatMessage.builder()
      .chatRoomId(chatRoom.getChatRoomId())
      .senderId(sender.getMemberId())
      .senderNickName(sender.getNickname())
      .senderEmail(sender.getUsername())
      .senderProfileImgStoredPath(sender.getProfileImgStoredPath())
      .message(message) // 거절 사유만 세팅
      .chatMessageType(type)
      .isRead(false)
      .sendDate(TimeUtil.now())
      .pictureMessageStoredPathList(Collections.emptyList())
      .referenceId(request.getFulfillmentFormId().toString())
      .build();

    return chatMessageRepository.save(entity);
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

  private Map<String, Object> buildUnreadTopicPayload(String chatRoomId, long unReadMessageCount, String lastMessageId,
    ChatMessageType lastMessageType, String lastMessage, String sendDate
  ) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("lastMessageId", lastMessageId);
    payload.put("lastMessageType", lastMessageType);
    payload.put("unReadMessageCount", unReadMessageCount);
    payload.put("chatRoomId", chatRoomId);
    payload.put("lastMessage", lastMessage == null ? "" : lastMessage);
    payload.put("sendDate", sendDate == null ? "" : sendDate);
    return payload;
  }


  private String toPreview(ChatMessage chatMessage) {
    return switch (chatMessage.getChatMessageType()) {
      case TEXT -> nvl(chatMessage.getMessage(), "");
      case PICTURE -> ChatMessageType.PICTURE.getDescription();
      case FULFILLMENT_FORM -> ChatMessageType.FULFILLMENT_FORM.getDescription();
      case ACCEPTED_FULFILLMENT_FORM -> ChatMessageType.ACCEPTED_FULFILLMENT_FORM.getDescription();
      case REJECTED_FULFILLMENT_FORM -> ChatMessageType.REJECTED_FULFILLMENT_FORM.getDescription();
      case UPDATE_FULFILLMENT_FORM -> ChatMessageType.UPDATE_FULFILLMENT_FORM.getDescription();
    };
  }
}