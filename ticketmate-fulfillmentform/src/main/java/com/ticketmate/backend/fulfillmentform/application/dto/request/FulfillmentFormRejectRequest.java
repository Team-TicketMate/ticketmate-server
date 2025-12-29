package com.ticketmate.backend.fulfillmentform.application.dto.request;

import static com.ticketmate.backend.common.core.constant.ValidationConstants.FulfillmentForm.REJECTED_MEMO_MAX_LENGTH;

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

  @Size(max = REJECTED_MEMO_MAX_LENGTH)
  @SizeErrorCode(ErrorCode.REJECT_MEMO_TOO_LONG)
  private String rejectedMemo;
}
