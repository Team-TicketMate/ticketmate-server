package com.ticketmate.backend.domain.search.domain.dto.request;

import com.ticketmate.backend.domain.search.domain.dto.constant.SearchType;
import com.ticketmate.backend.global.constant.PageableConstants;
import com.ticketmate.backend.global.util.database.PageableUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Pageable;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SearchRequest {
  private String keyword; // 검색 키워드

  @Builder.Default
  private SearchType type = SearchType.CONCERT; // 검색 타입

  @Schema(defaultValue = "1")
  @Min(value = 1, message = "페이지 번호는 1이상 값을 입력해야합니다.")
  @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
  @Builder.Default
  private Integer pageNumber = 1; // 페이지 번호 (1부터 시작)

  @Schema(defaultValue = "10")
  @Min(value = 1, message = "페이지 당 데이터 최솟값은 1개 입니다.")
  @Max(value = PageableConstants.MAX_PAGE_SIZE, message = "페이지 당 데이터 최댓값은 " + PageableConstants.MAX_PAGE_SIZE + "개 입니다.")
  @Builder.Default
  private Integer pageSize = PageableConstants.DEFAULT_PAGE_SIZE; // 페이지 사이즈

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
