package com.ticketmate.backend.fulfillmentform.application.dto.request;

import static com.ticketmate.backend.fulfillmentform.infrastructure.constant.FulfillmentFormConstants.MAX_REJECTED_MEMO_LENGTH;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FulfillmentFormRejectRequest {

  @Size(max = MAX_REJECTED_MEMO_LENGTH)
  @SizeErrorCode(ErrorCode.REJECT_MEMO_TOO_LONG)
  private String rejectedMemo;
}
