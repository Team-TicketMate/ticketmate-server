package com.ticketmate.backend.fulfillmentform.infrastructure.constant;

import com.ticketmate.backend.fulfillmentform.core.constant.FulfillmentFormStatus;
import java.util.EnumSet;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FulfillmentFormConstants {
  // 수정 가능한 신청서 상태
  public static final EnumSet<FulfillmentFormStatus> UPDATABLE_STATUSES =
    EnumSet.of(FulfillmentFormStatus.PENDING_FULFILLMENT_FORM,
      FulfillmentFormStatus.UPDATE_FULFILLMENT_FORM,
      FulfillmentFormStatus.REJECTED_FULFILLMENT_FORM);
}
