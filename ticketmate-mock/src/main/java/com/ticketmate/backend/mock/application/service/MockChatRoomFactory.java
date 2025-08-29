package com.ticketmate.backend.mock.application.service;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockChatRoomFactory {
  public ChatRoom generateFrom(ApplicationForm applicationForm) {
    if (applicationForm.getClient() == null || applicationForm.getAgent() == null
        || applicationForm.getConcert() == null || applicationForm.getTicketOpenType() == null) {
      log.error("ChatRoom 생성에 필요한 필드가 누락되었습니다. applicationFormId={}", applicationForm.getApplicationFormId());
      throw new CustomException(ErrorCode.GENERATE_MOCK_DATA_ERROR);
    }

    return ChatRoom.builder()
        .agentMemberId(applicationForm.getAgent().getMemberId())
        .clientMemberId(applicationForm.getClient().getMemberId())
        .agentMemberNickname(applicationForm.getAgent().getNickname())
        .clientMemberNickname(applicationForm.getClient().getNickname())
        .applicationFormId(applicationForm.getApplicationFormId())
        .concertId(applicationForm.getConcert().getConcertId())
        .ticketOpenType(applicationForm.getTicketOpenType())
        .build();
  }

  /**
   * 개별 파라미터로 채팅방 도큐먼트를 생성합니다. (저장 X)
   */
  public ChatRoom generate(Member agent, Member client, Concert concert,
      TicketOpenType ticketOpenType, UUID applicationFormId) {
    if (agent == null || client == null || concert == null || ticketOpenType == null || applicationFormId == null) {
      log.error("ChatRoom 생성 파라미터 누락: agent/client/concert/openType/applicationFormId 필수");
      throw new CustomException(ErrorCode.GENERATE_MOCK_DATA_ERROR);
    }

    return ChatRoom.builder()
        .agentMemberId(agent.getMemberId())
        .clientMemberId(client.getMemberId())
        .agentMemberNickname(agent.getNickname())
        .clientMemberNickname(client.getNickname())
        .applicationFormId(applicationFormId)
        .concertId(concert.getConcertId())
        .ticketOpenType(ticketOpenType)
        .build();
  }
}
