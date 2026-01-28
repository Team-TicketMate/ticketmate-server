package com.ticketmate.backend.chat.application.dto.response;

import com.ticketmate.backend.concert.application.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatRoomContextResponse {

  private String chatRoomId;  // 채팅방 ID
  private UUID opponentMemberId;  // 상대방 ID
  private UUID fulfillmentFormId;  // 성공양식 Id
  private String opponentMemberNickName;  // 상대방 닉네임
  private String concertName;  // 콘서트명
  private String concertThumbnailUrl;  // 콘서트 썸네일 이미지
  private TicketOpenType ticketOpenType;  // 선예/일예 구분
  private List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList; // 티켓 오픈일 List
  private TicketReservationSite ticketReservationSite;  // 예매처
  private ConcertType concertType;  // 공연 카테고리
  private boolean chatEnabled;  // 현재 채팅이 가능한지
  private boolean opponentLeft;  // 상대방이 나갔는지

  @Builder
  public ChatRoomContextResponse(String chatRoomId, UUID opponentMemberId, UUID fulfillmentFormId, String opponentMemberNickName, String concertName, String concertThumbnailUrl,
    TicketOpenType ticketOpenType, List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList, TicketReservationSite ticketReservationSite, ConcertType concertType, boolean chatEnabled,
    boolean opponentLeft) {
    this.chatRoomId = chatRoomId;
    this.opponentMemberId = opponentMemberId;
    this.fulfillmentFormId = fulfillmentFormId;
    this.opponentMemberNickName = opponentMemberNickName;
    this.concertName = concertName;
    this.concertThumbnailUrl = concertThumbnailUrl;
    this.ticketOpenType = ticketOpenType;
    this.ticketOpenDateInfoResponseList = ticketOpenDateInfoResponseList;
    this.ticketReservationSite = ticketReservationSite;
    this.concertType = concertType;
    this.chatEnabled = chatEnabled;
    this.opponentLeft = opponentLeft;
  }
}
