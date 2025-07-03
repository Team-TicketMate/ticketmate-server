package com.ticketmate.backend.global.constant;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationFormConstants {

  public static final int APPLICATION_FORM_MIN_REQUEST_COUNT = 1;
  public static final int APPLICATION_FORM_MAX_REQUEST_COUNT = 10;

  // 수정 가능한 신청서 상태
  public static final List<ApplicationFormStatus> EDITABLE_APPLICATION_FORM_STATUS = List.of(
      ApplicationFormStatus.CANCELED,
      ApplicationFormStatus.REJECTED,
      ApplicationFormStatus.CANCELED_IN_PROCESS
  );
}
