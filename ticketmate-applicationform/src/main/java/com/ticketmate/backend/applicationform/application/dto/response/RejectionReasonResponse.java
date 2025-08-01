package com.ticketmate.backend.applicationform.application.dto.response;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormRejectedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RejectionReasonResponse {

  private ApplicationFormRejectedType applicationFormRejectedType;

  private String otherMemo;
}
