package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자가 채팅방 검색시 사용될 DTO
 * 필터링 종류 = 키워드 검색, 선예매만 보기, 일반예매만 보기
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomFilteredRequest {

  @Size(max = 30)
  @SizeErrorCode(ErrorCode.SEARCH_KEYWORD_TOO_LONG)
  private String searchKeyword;  // 검색 키워드

  private TicketOpenType ticketOpenType;  // 선예매 일반예매

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PAGE_NUMBER_TOO_SMALL)
  private Integer pageNumber;  // 페이지 번호 (1부터 시작)
}
