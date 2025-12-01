package com.ticketmate.backend.fulfillmentform.infrastructure.constant;

import com.ticketmate.backend.fulfillmentform.core.constant.FulfillmentFormStatus;
import java.util.EnumSet;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FulfillmentFormConstants {

  // 성공 사진 이미지 최대 개수
  public static final int MAX_IMG_COUNT = 6;

  // 상세설명 최대 길이
  public static final int MAX_PARTICULAR_MEMO_LENGTH = 100;

  // 거절사유 최대 길이
  public static final int MAX_REJECTED_MEMO_LENGTH = 100;

  // 수정 가능한 신청서 상태
  public static final EnumSet<FulfillmentFormStatus> UPDATABLE_STATUSES =
    EnumSet.of(FulfillmentFormStatus.PENDING_FULFILLMENT_FORM,
      FulfillmentFormStatus.UPDATE_FULFILLMENT_FORM,
      FulfillmentFormStatus.REJECTED_FULFILLMENT_FORM);
}
