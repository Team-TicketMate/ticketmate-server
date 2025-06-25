package com.ticketmate.backend.domain.chat.service;

import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.repository.ApplicationFormRepository;
import com.ticketmate.backend.domain.chat.domain.dto.request.ChatRoomFilteredRequest;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatMessageResponse;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatRoomListResponse;
import com.ticketmate.backend.domain.chat.domain.entity.ChatMessage;
import com.ticketmate.backend.domain.chat.domain.entity.ChatRoom;
import com.ticketmate.backend.domain.chat.repository.ChatMessageRepository;
import com.ticketmate.backend.domain.chat.repository.ChatRoomRepository;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.repository.MemberRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.PageableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ticketmate.backend.global.exception.ErrorCode.APPLICATION_FORM_NOT_FOUND;
import static com.ticketmate.backend.global.exception.ErrorCode.CHAT_ROOM_NOT_FOUND;
import static com.ticketmate.backend.global.util.common.CommonUtil.nvl;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

  private static final String UN_READ_MESSAGE_COUNTER_KEY = "unRead:%s:%s";
  private final ChatRoomRepository chatRoomRepository;
  private final MemberRepository memberRepository;
  private final ApplicationFormRepository applicationFormRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final EntityMapper entityMapper;
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 사용자별 존재하는 채팅방 리스트를 불러오는 메서드입니다.
   */
  @Transactional(readOnly = true)
  public Page<ChatRoomListResponse> getChatRoomList(Member member, ChatRoomFilteredRequest request) {
    // 검색 키워드 관련 필드값 세팅
    String keyword = nvl(request.getSearchKeyword(), "");

    // PageableUtil을 사용하여 1부터 시작하는 페이지 번호를 인덱스로 변환
    int pageIndex = PageableUtil.convertToPageIndex(request.getPageNumber());

    // 채팅방 리스트 불러오기 (10개씩)
    Page<ChatRoom> chatRoomList = chatRoomRepository.search(request.getTicketOpenType(), keyword, member, pageIndex);

    // 채팅방마다 존재하는 신청폼 Id 리스트에 저장
    List<UUID> applicationFormIdList = chatRoomList.stream()
        .map(ChatRoom::getApplicationFormId).collect(Collectors.toList());

    // 채팅방마다 존재하는 사용자 ID중 상대방 ID만 빼와서 리스트에 저장
    Set<UUID> opponentIdList = chatRoomList.stream()
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
      chatRoomList.forEach(r -> {
        String redisKey = (UN_READ_MESSAGE_COUNTER_KEY).formatted(r.getChatRoomId(), member.getMemberId());
        con.stringCommands().get(redisKey.getBytes());
      });
      return null;
    });

    AtomicInteger i = new AtomicInteger();
    List<ChatRoomListResponse> response = chatRoomList.stream()
        .map(r -> {
          int unReadMessageCount = parseInt(countList.get(i.getAndIncrement()));
          return chatRoomRepository.toResponse(
              r, member, applicationFormMap, memberMap, unReadMessageCount);
        })
        .toList();

    return new PageImpl<>(response, chatRoomList.getPageable(), chatRoomList.getTotalElements());
  }

  /**
   * 현재 진행한 채팅 메시지 불러오는 메서드
   */
  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getChatMessage(Member member, String chatRoomId) {
    ChatRoom room = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));
    validateRoomMember(room, member);

    List<ChatMessage> massageList = chatMessageRepository
        .findByChatRoomIdOrderBySendDateAsc(chatRoomId);

    return massageList.stream().map(entityMapper::toChatMessageResponse).toList();
  }

  /**
   * 방 참가자 검증
   */
  private void validateRoomMember(ChatRoom room, Member member) {
    UUID id = member.getMemberId();
    if (!id.equals(room.getAgentMemberId()) && !id.equals(room.getClientMemberId())) {
      throw new CustomException(ErrorCode.NO_AUTH_TO_ROOM);
    }
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
   *
   * @param chatRoomId  채팅방 고유 ID
   * @return  현재 진행중인 신청폼 정보 (1:N , 콘서트 :회차)
   */
  @Transactional(readOnly = true)
  public ApplicationFormFilteredResponse getChatRoomApplicationFormInfo(Member member, String chatRoomId) {

    // 요청된 채팅방 추출
    ChatRoom chatRoom = chatRoomRepository
            .findById(chatRoomId).orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

    // 현재 사용자 검증
    validateRoomMember(chatRoom, member);

    // 메서드 체이닝
    return Optional.of(chatRoom)
            .map(ChatRoom::getApplicationFormId)
            .flatMap(applicationFormRepository::findById)
            .map(entityMapper::toApplicationFormFilteredResponse)
            .orElseThrow(() -> new CustomException(APPLICATION_FORM_NOT_FOUND));
  }
}
