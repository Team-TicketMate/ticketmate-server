package com.ticketmate.backend.concertagentavailability.application.dto.request;

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

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AgentConcertSettingFilteredRequest {

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PAGE_NUMBER_TOO_SMALL)
  @Max(value = Integer.MAX_VALUE)
  @MaxErrorCode(ErrorCode.PAGE_NUMBER_TOO_LARGE)
  private Integer pageNumber; // 페이지 번호 (1부터 시작)

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PAGE_SIZE_TOO_SMALL)
  @Max(value = PageableConstants.MAX_PAGE_SIZE)
  @MaxErrorCode(ErrorCode.PAGE_SIZE_TOO_LARGE)
  private Integer pageSize; // 페이지 사이즈

  public AgentConcertSettingFilteredRequest() {
    this.pageNumber = 1;
    this.pageSize = PageableConstants.DEFAULT_PAGE_SIZE;
  }

  public Pageable toPageable() {
    return PageableUtil.createPageable(
        pageNumber,
        pageSize,
        PageableConstants.DEFAULT_PAGE_SIZE,
        null,
        null
    );
  }
}