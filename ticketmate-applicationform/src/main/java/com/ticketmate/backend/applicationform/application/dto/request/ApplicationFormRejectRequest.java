package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import jakarta.validation.constraints.NotNull;
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
public class ApplicationFormRejectRequest {

  @NotNull
  @NotNullErrorCode(ErrorCode.APPLICATION_FORM_REJECTED_TYPE_EMPTY)
  private ApplicationFormRejectedType applicationFormRejectedType;  // 거절 사유

  private String otherMemo;  // 사유가 '기타' 일경우 받아올 메모
}