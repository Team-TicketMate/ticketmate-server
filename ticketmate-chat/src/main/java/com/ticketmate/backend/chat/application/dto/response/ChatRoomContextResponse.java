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
  private UUID otherMemberId;  // 상대방 ID
  private String concertName;  // 콘서트명
  private String concertThumbnailImage;  // 콘서트 썸네일 이미지
  private TicketOpenType ticketOpenType;  // 선예/일예 구분
  private List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList; // 티켓 오픈일 List
  private TicketReservationSite ticketReservationSite;  // 예매처
  private ConcertType concertType;  // 공연 카테고리

  @Builder
  public ChatRoomContextResponse(String chatRoomId, UUID otherMemberId, String concertName, String concertThumbnailImage, TicketOpenType ticketOpenType,
      List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList, TicketReservationSite ticketReservationSite, ConcertType concertType) {
    this.chatRoomId = chatRoomId;
    this.otherMemberId = otherMemberId;
    this.concertName = concertName;
    this.concertThumbnailImage = concertThumbnailImage;
    this.ticketOpenType = ticketOpenType;
    this.ticketOpenDateInfoResponseList = ticketOpenDateInfoResponseList;
    this.ticketReservationSite = ticketReservationSite;
    this.concertType = concertType;
  }
}
