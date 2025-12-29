package com.ticketmate.backend.search.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import com.ticketmate.backend.common.infrastructure.constant.PageableConstants;
import com.ticketmate.backend.common.infrastructure.util.PageableUtil;
import com.ticketmate.backend.search.core.constant.SearchType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

  @NotBlank
  @NotBlankErrorCode(ErrorCode.KEYWORD_EMPTY)
  @Size(max = 20)
  @SizeErrorCode(ErrorCode.KEYWORD_TOO_LONG)
  private String keyword; // 검색 키워드

  @NotNull
  @NotNullErrorCode(ErrorCode.SEARCH_TYPE_EMPTY)
  private SearchType searchType; // 검색 타입

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PAGE_NUMBER_TOO_SMALL)
  @Max(value = Integer.MAX_VALUE)
  @MaxErrorCode(ErrorCode.PAGE_NUMBER_TOO_LARGE)
  @Builder.Default
  private Integer pageNumber = 1; // 페이지 번호 (1부터 시작)

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PAGE_SIZE_TOO_SMALL)
  @Max(value = PageableConstants.MAX_PAGE_SIZE)
  @MaxErrorCode(ErrorCode.PAGE_SIZE_TOO_LARGE)
  private Integer pageSize; // 페이지 사이즈

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
