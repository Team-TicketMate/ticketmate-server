package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormSortField;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.infrastructure.constant.PageableConstants;
import com.ticketmate.backend.common.infrastructure.util.PageableUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;
import java.util.UUID;
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
public class ApplicationFormFilteredRequest {

  private UUID clientId; // 의뢰인 PK

  private UUID agentId; // 대리인 PK

  private UUID concertId; // 공연 PK

  private Set<ApplicationFormStatus> applicationFormStatusSet; // 신청서 상태 Set<>

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

  private ApplicationFormSortField sortField; // 정렬 조건

  private Sort.Direction sortDirection; // 정렬 방향

  // 기본값 할당 (1페이지 10개, 최신순)
  public ApplicationFormFilteredRequest() {
    this.pageNumber = 1;
    this.pageSize = PageableConstants.DEFAULT_PAGE_SIZE;
    this.sortField = ApplicationFormSortField.CREATED_DATE;
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
