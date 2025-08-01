package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormRejectedType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationFormRejectRequest {

  @NotNull
  private ApplicationFormRejectedType applicationFormRejectedType;  // 거절 사유

  private String otherMemo;  // 사유가 '기타' 일경우 받아올 메모
}