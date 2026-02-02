package com.ticketmate.backend.chat.application.service;

import static com.ticketmate.backend.chat.infrastructure.constant.ChatConstants.UN_READ_MESSAGE_COUNTER_KEY;
import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;

import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.applicationform.application.service.ApplicationFormService;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.applicationform.infrastructure.repository.ApplicationFormRepository;
import com.ticketmate.backend.chat.application.dto.request.ChatMessageFilteredRequest;
import com.ticketmate.backend.chat.application.dto.request.ChatRoomFilteredRequest;
import com.ticketmate.backend.chat.application.dto.response.ChatMessageResponse;
import com.ticketmate.backend.chat.application.dto.response.ChatRoomResponse;
import com.ticketmate.backend.chat.application.mapper.ChatMapper;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.chat.infrastructure.repository.ChatMessageRepository;
import com.ticketmate.backend.chat.infrastructure.repository.ChatRoomRepository;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.util.PageableUtil;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;
  private final MemberRepository memberRepository;
  private final ApplicationFormService applicationFormService;
  private final ApplicationFormRepository applicationFormRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ChatMapper chatMapper;
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 자신이 아닌 상대방의 id를 찾아주는 메서드입니다.
   *
   * @param room   채팅방 객체
   * @param member 현재 사용자
   * @return 상대방의 고유 ID
   */
  public static UUID opponentIdOf(ChatRoom room, Member member) {
    // 여기서 member는 자기 자신입니다.
    return room.getAgentMemberId().equals(member.getMemberId())
      ? room.getClientMemberId()
      : room.getAgentMemberId();
  }

  /**
   * 수락된 신청서 PK를 통해 새로운 채팅방 생성
   * 채팅방이 존재하는경우 기존 채팅방 PK 반환
   *
   * @param applicationFormId 신청서 PK
   * @return chatRoomId
   */
  @Transactional
  public String generateChatRoom(UUID applicationFormId) {
    ApplicationForm applicationForm = applicationFormService.findApplicationFormById(applicationFormId);
    Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByApplicationFormId(applicationFormId);

    if (chatRoomOpt.isPresent()) {
      ChatRoom chatRoom = chatRoomOpt.get();
      log.warn("신청서: {}에 대한 채팅방: {}이 이미 존재합니다", applicationForm.getApplicationFormId(), chatRoom.getChatRoomId());
      return chatRoom.getChatRoomId();
    }

    Member agent = applicationForm.getAgent();
    Member client = applicationForm.getClient();
    Concert concert = applicationForm.getConcert();

    ChatRoom chatRoom = ChatRoom.builder()
      .agentMemberId(agent.getMemberId())
      .agentMemberNickname(agent.getNickname())
      .clientMemberId(client.getMemberId())
      .clientMemberNickname(client.getNickname())
      .lastMessage("")
      .lastMessageId("")
      .applicationFormId(applicationForm.getApplicationFormId())
      .concertId(concert.getConcertId())
      .ticketOpenType(applicationForm.getTicketOpenType())
      .build();
    ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
    return savedChatRoom.getChatRoomId();
  }

  /**
   * 사용자별 존재하는 채팅방 리스트를 불러오는 메서드입니다.
   */
  @Transactional(readOnly = true)
  public Page<ChatRoomResponse> getChatRoomList(Member member, ChatRoomFilteredRequest request) {
    // 검색 키워드 관련 필드값 세팅
    String keyword = nvl(request.getSearchKeyword(), "");

    // PageableUtil을 사용하여 1부터 시작하는 페이지 번호를 인덱스로 변환
    int pageIndex = PageableUtil.convertToPageIndex(request.getPageNumber());

    // 채팅방 리스트 불러오기 (10개씩)
    Page<ChatRoom> chatRoomPage = chatRoomRepository.search(request.getTicketOpenType(), keyword, member, pageIndex);

    // 채팅방마다 존재하는 신청폼 Id 리스트에 저장
    List<UUID> applicationFormIdList = chatRoomPage.stream()
      .map(ChatRoom::getApplicationFormId).collect(Collectors.toList());

    // 채팅방마다 존재하는 사용자 ID중 상대방 ID만 빼와서 리스트에 저장
    Set<UUID> opponentIdList = chatRoomPage.stream()
      .map(room -> opponentIdOf(room, member))
      .collect(Collectors.toSet());

    // 채팅방에 존재하는 신청폼 한번에 조회 (N+1 방지)
    Map<UUID, ApplicationForm> applicationFormMap = applicationFormRepository
      .findAllById(applicationFormIdList)
      .stream()
      .collect(Collectors.toMap(ApplicationForm::getApplicationFormId, Function.identity()));

    // 채팅방에 존재하는 회원 한번에 조회 (N+1 방지)
    Map<UUID, Member> memberMap = memberRepository
      .findAllById(opponentIdList)
      .stream()
      .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

    List<Object> countList = redisTemplate.executePipelined((RedisCallback<Object>) con -> {
      chatRoomPage.forEach(r -> {
        String redisKey = (UN_READ_MESSAGE_COUNTER_KEY).formatted(r.getChatRoomId(), member.getMemberId());
        con.stringCommands().get(redisKey.getBytes());
      });
      return null;
    });

    AtomicInteger i = new AtomicInteger();
    List<ChatRoomResponse> response = chatRoomPage.stream()
      .map(r -> {
        int unReadMessageCount = parseInt(countList.get(i.getAndIncrement()));
        return chatMapper.toChatRoomResponse(
          r, member, applicationFormMap, memberMap, unReadMessageCount);
      })
      .toList();

    return new PageImpl<>(response, chatRoomPage.getPageable(), chatRoomPage.getTotalElements());
  }

  /**
   * 현재 진행한 채팅 메시지 불러오는 메서드
   */
  @Transactional(readOnly = true)
  public Slice<ChatMessageResponse> getChatMessage(Member member, String chatRoomId, ChatMessageFilteredRequest request) {
    // 메시지를 조회할 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
      .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

    // 채팅방 내부 참가자 유효성 검증 및 채팅방 유효성 검증
    validateActiveParticipant(chatRoom, member);

    // 채팅메시지 전용 페이지네이션 객체 생성
    Pageable pageable = request.toPageable();

    return chatMessageRepository
      .findByChatRoomId(chatRoomId, pageable)
      .map(chatMessage -> chatMapper.toChatMessageResponse(chatMessage, member.getMemberId()));
  }

  /**
   * @param chatRoomId 채팅방 고유 ID
   * @return 현재 진행중인 신청폼 정보 (1:N , 콘서트 :회차)
   */
  @Transactional(readOnly = true)
  public ApplicationFormInfoResponse getChatRoomApplicationFormInfo(Member member, String chatRoomId) {
    // 요청된 채팅방 추출
    ChatRoom chatRoom = chatRoomRepository
      .findById(chatRoomId).orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

    // 현재 사용자 검증
    validateRoomMember(chatRoom, member);

    // 메서드 체이닝
    return Optional.of(chatRoom)
      .map(ChatRoom::getApplicationFormId)
      .map(applicationFormService::getApplicationFormInfo)
      .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_FORM_NOT_FOUND));
  }

  /**
   * 채팅까지 진행된 후 진행취소를 위한 API
   *
   * @param member     (진행 취소를 요청한 회원)
   * @param chatRoomId (현재 채팅방 ID)
   */
  @Transactional
  public void cancelProgress(Member member, String chatRoomId) {
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> {
      log.error("진행 취소를 위한 채팅방을 찾지 못했습니다.");
      return new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
    });

    validateRoomMember(chatRoom, member);

    ApplicationForm applicationForm = applicationFormRepository.findById(chatRoom.getApplicationFormId()).orElseThrow(() -> {
      log.error("요청한 채팅방 {} 에 대한 신청서를 찾지 못했습니다.", chatRoom.getChatRoomId());
      return new CustomException(ErrorCode.APPLICATION_FORM_NOT_FOUND);
    });

    // 신청서의 불변 보장(추후 신청내역 조회를 위해)을 위해 상태만 변경
    applicationForm.setApplicationFormStatus(ApplicationFormStatus.CANCELED_IN_PROCESS);
  }

  // 채팅방 나가기 기능
  @Transactional
  public void leaveChatRoom(Member member, String chatRoomId) {
    ChatRoom chatRoom = findChatRoomById(chatRoomId);
    validateRoomMember(chatRoom, member);

    UUID memberId = member.getMemberId();
    if (chatRoom.isLeft(memberId)) {
      return;
    }

    chatRoom.leave(memberId, Instant.now());
    chatRoomRepository.save(chatRoom);

    // unread 키 삭제
    String key = (UN_READ_MESSAGE_COUNTER_KEY).formatted(chatRoomId, memberId);
    redisTemplate.delete(key);
    log.debug("채팅방 나가기 완료. 채팅방 상태 : {}, 나간 시간: {}", chatRoom.getRoomStatus(), chatRoom.getClosedDate());
  }

  /**
   * 방 참가자 검증
   */
  public void validateRoomMember(ChatRoom room, Member member) {
    UUID id = member.getMemberId();
    if (!id.equals(room.getAgentMemberId()) && !id.equals(room.getClientMemberId())) {
      log.error("현재 사용자는 해당 채팅방에 대한 권한이 없습니다.");
      throw new CustomException(ErrorCode.NO_AUTH_TO_ROOM);
    }
  }

  private void validateActiveParticipant(ChatRoom room, Member member) {
    validateRoomMember(room, member);

    if (room.isLeft(member.getMemberId())) {
      throw new CustomException(ErrorCode.CHAT_ROOM_LEFT);
    }
  }

  /**
   * 채팅방 조회 공용 메서드
   */
  public ChatRoom findChatRoomById(String chatRoomId) {
    return chatRoomRepository.findById(chatRoomId).orElseThrow(
      () -> {
        log.error("채팅방을 찾지 못했습니다. 요청받은 채팅방 ID : {}", chatRoomId);
        throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
      }
    );
  }

  /**
   * 형변환 메서드
   */
  private int parseInt(Object object) {
    if (object == null) {
      return 0;
    }
    if (object instanceof byte[] b) {
      return Integer.parseInt(new String(b));
    }
    return Integer.parseInt(object.toString());
  }
}
