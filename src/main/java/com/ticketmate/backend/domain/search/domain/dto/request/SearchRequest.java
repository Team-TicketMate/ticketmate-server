package com.ticketmate.backend.domain.search.domain.dto.request;

import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import com.ticketmate.backend.global.constant.PageableConstants;
import com.ticketmate.backend.global.util.database.PageableUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Pageable;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SearchRequest {
  private String keyword; // 검색 키워드

  private EmbeddingType type = EmbeddingType.CONCERT; // 검색 타입

  @Schema(defaultValue = "1")
  @Min(value = 1, message = "페이지 번호는 1이상 값을 입력해야합니다.")
  private Integer pageNumber = 1;

  @Schema(defaultValue = "10")
  @Min(value = 1, message = "페이지 당 데이터 최솟값은 1개 입니다.")
  @Max(value = PageableConstants.MAX_PAGE_SIZE, message = "페이지 당 데이터 최댓값은 " + PageableConstants.MAX_PAGE_SIZE + "개 입니다.")
  private Integer pageSize = PageableConstants.DEFAULT_PAGE_SIZE;

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
