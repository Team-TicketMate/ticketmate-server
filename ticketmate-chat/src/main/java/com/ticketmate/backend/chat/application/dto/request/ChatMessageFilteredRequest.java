package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageSortField;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.infrastructure.constant.PageableConstants;
import com.ticketmate.backend.common.infrastructure.util.PageableUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChatMessageFilteredRequest {

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PAGE_NUMBER_TOO_SMALL)
  @Max(value = Integer.MAX_VALUE)
  @MaxErrorCode(ErrorCode.PAGE_NUMBER_TOO_LARGE)
  private Integer pageNumber; // 페이지 번호 (1부터 시작)

  @Min(value = 20)
  @MinErrorCode(ErrorCode.CHAT_MESSAGE_PAGE_SIZE_TOO_SMALL)
  @Max(value = PageableConstants.MAX_PAGE_SIZE)
  @MaxErrorCode(ErrorCode.CHAT_MESSAGE_PAGE_SIZE_TOO_LARGE)
  private Integer pageSize; // 페이지 사이즈

  private ChatMessageSortField sortField; // 정렬 필드

  private Sort.Direction sortDirection; // 정렬 방향

  public ChatMessageFilteredRequest() {
    this.pageNumber = 1;
    this.pageSize = PageableConstants.CHAT_MESSAGE_DEFAULT_PAGE_SIZE;
    this.sortField = ChatMessageSortField.CREATED_DATE;
    this.sortDirection = Direction.DESC;
  }

  public Pageable toPageable() {
    return PageableUtil.createPageable(
        pageNumber,
        pageSize,
        PageableConstants.CHAT_MESSAGE_DEFAULT_PAGE_SIZE,
        sortField,
        sortDirection
    );
  }
}
