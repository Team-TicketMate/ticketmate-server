package com.ticketmate.backend.domain.applicationform.domain.dto.request;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.global.constant.PageableConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormFilteredRequest {

  private UUID clientId; // 의뢰인 PK

  private UUID agentId; // 대리인 PK

  private UUID concertId; // 공연 PK

  private Set<ApplicationFormStatus> applicationFormStatusSet; // 신청서 상태 Set<>

  @Schema(defaultValue = "1")
  @Min(value = 1, message = "페이지 번호는 1이상 값을 입력해야합니다.")
  @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
  private Integer pageNumber; // 페이지 번호 (1부터 시작)

  @Schema(defaultValue = "10")
  @Min(value = 1, message = "페이지 당 데이터 최솟값은 1개 입니다.")
  @Max(value = PageableConstants.MAX_PAGE_SIZE, message = "페이지 당 데이터 최댓값은 " + PageableConstants.MAX_PAGE_SIZE + "개 입니다.")
  private Integer pageSize; // 페이지 사이즈

  @Schema(defaultValue = "created_date")
  @Pattern(regexp = "^(createdDate|requestCount)$")
  private String sortField; // 정렬 조건 (생성일, 매수)

  @Schema(defaultValue = "DESC")
  @Pattern(regexp = "^(ASC|DESC)$", message = "sortDirection에는 'ASC', 'DESC' 만 입력 가능합니다.")
  private String sortDirection; // ASC, DESC

  // 기본값 할당 (1페이지 30개, 최신순)
  public ApplicationFormFilteredRequest() {
    this.pageNumber = 1;
    this.pageSize = PageableConstants.DEFAULT_PAGE_SIZE;
    this.sortField = PageableConstants.DEFAULT_SORT_FIELD;
    this.sortDirection = PageableConstants.DEFAULT_SORT_DIRECTION;
  }
}
