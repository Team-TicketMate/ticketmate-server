package com.ticketmate.backend.fulfillmentform.application.dto.successhistory.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.infrastructure.constant.PageableConstants;
import com.ticketmate.backend.common.infrastructure.util.PageableUtil;
import com.ticketmate.backend.fulfillmentform.core.constant.successhistory.SuccessHistorySortField;
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
public class SuccessHistoryFilteredRequest {

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PAGE_NUMBER_TOO_SMALL)
  @Max(value = Integer.MAX_VALUE)
  @MaxErrorCode(ErrorCode.PAGE_NUMBER_TOO_LARGE)
  private Integer pageNumber; // 페이지 번호 (1부터 시작)

  @Min(PageableConstants.DEFAULT_PAGE_SIZE)
  @MinErrorCode(ErrorCode.SUCCESS_HISTORY_PAGE_SIZE_TOO_SMALL)
  @Max(value = PageableConstants.MAX_PAGE_SIZE)
  @MaxErrorCode(ErrorCode.SUCCESS_HISTORY_PAGE_SIZE_TOO_LARGE)
  private Integer pageSize; // 페이지 사이즈

  private SuccessHistorySortField sortField; // 정렬 필드

  private Sort.Direction sortDirection; // 정렬 방향

  public SuccessHistoryFilteredRequest() {
    this.pageNumber = 1;
    this.pageSize = PageableConstants.DEFAULT_PAGE_SIZE;
    this.sortField = SuccessHistorySortField.CREATED_DATE;
    this.sortDirection = Direction.DESC;
  }

  public Pageable toPageable() {
    return PageableUtil.createPageable(
      pageNumber,
      pageSize,
      PageableConstants.DEFAULT_PAGE_SIZE,
      sortField,
      sortDirection
    );
  }
}
