package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotEmptyErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFormEditRequest {

  @Valid
  @NotEmpty
  @NotEmptyErrorCode(ErrorCode.APPLICATION_FORM_DETAIL_REQUEST_LIST_EMPTY)
  private List<ApplicationFormDetailRequest> applicationFormDetailRequestList;
}
