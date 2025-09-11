package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageSortField;
import com.ticketmate.backend.common.infrastructure.constant.PageableConstants;
import com.ticketmate.backend.common.infrastructure.util.PageableUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChatMessageFilteredRequest {

  @Min(value = 1, message = "페이지 번호는 1이상 값을 입력해야합니다.")
  @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
  private Integer pageNumber; // 페이지 번호 (1부터 시작)

  @Min(value = 20, message = "체팅메시지당 데이터 최솟값은 20개 입니다.")
  @Max(value = PageableConstants.MAX_PAGE_SIZE, message = "체팅메시지당 데이터 최댓값은 " + PageableConstants.MAX_PAGE_SIZE + "개 입니다.")
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
