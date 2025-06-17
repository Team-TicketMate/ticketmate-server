package com.ticketmate.backend.object.dto.expressions.request;

import com.ticketmate.backend.object.constants.ApplicationFormRejectedType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationFormRejectRequest {

  private ApplicationFormRejectedType applicationFormRejectedType;  // 거절 사유

  private String otherMemo;  // 사유가 '기타' 일경우 받아올 메모
}