package com.ticketmate.backend.api.application.service;

import com.ticketmate.backend.chat.application.dto.response.ChatRoomContextResponse;
import com.ticketmate.backend.chat.application.mapper.ChatMapper;
import com.ticketmate.backend.chat.application.service.ChatRoomService;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.chat.infrastructure.repository.ChatRoomRepository;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.service.ConcertService;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.fulfillmentform.infrastructure.repository.FulfillmentFormRepository;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DTO 필드값 추가로 어쩔 수 없는 순환 참조 문제때문에 문제가 발생하는 메서드를 상위 모듈로 올려 파사드 패턴 적용
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnterChatRoomService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomService chatRoomService;
  private final ConcertService concertService;
  private final MemberService memberService;
  private final FulfillmentFormRepository fulfillmentFormRepository;
  private final ChatMapper chatMapper;

  /**
   * 채팅방 입장시 반환하는 데이터
   */
  @Transactional(readOnly = true)
  public ChatRoomContextResponse enterChatRoom(Member member, String chatRoomId) {
    // 입장할 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
      .orElseThrow(() -> {
          log.error("채팅방 조회에 실패했습니다.");
          return new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
      );

    // 채팅방 내부 참가자 유효성 검증
    chatRoomService.validateRoomMember(chatRoom, member);

    // 콘서트 정보 조회
    UUID concertId = chatRoom.getConcertId();
    ConcertInfoResponse response = concertService.getConcertInfo(concertId);

    // 상대방 닉네임 추출
    UUID opponentId = chatRoom.getOpponentId(member.getMemberId());
    Member opponentMember = memberService.findMemberById(opponentId);

    // 성공양식 조회 (없다면 null 반환)
    UUID fulfillmentFormId = fulfillmentFormRepository.findByChatRoomId(chatRoomId)
      .map(FulfillmentForm::getFulfillmentFormId)
      .orElse(null);

    return chatMapper.toChatRoomContextResponse(chatRoom, member.getMemberId(), fulfillmentFormId,
      chatRoom.getTicketOpenType(), opponentMember.getNickname(), response);
  }
}
