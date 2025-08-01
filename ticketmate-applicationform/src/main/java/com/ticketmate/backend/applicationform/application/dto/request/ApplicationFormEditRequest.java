package com.ticketmate.backend.applicationform.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormEditRequest {

  @Valid
  @NotEmpty(message = "신청서에는 최소 1개 이상의 신청서 세부사항이 포함되어야 합니다")
  private List<ApplicationFormDetailRequest> applicationFormDetailRequestList;
}
