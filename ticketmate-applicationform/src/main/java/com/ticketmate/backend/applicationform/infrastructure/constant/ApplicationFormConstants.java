package com.ticketmate.backend.applicationform.infrastructure.constant;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationFormConstants {

  // 요청 매수 constraints
  public static final int APPLICATION_FORM_MIN_REQUEST_COUNT = 1;
  public static final int APPLICATION_FORM_MAX_REQUEST_COUNT = 10;

  // 요청사항 최대 길이
  public static final int REQUIREMENT_MAX_LENGTH = 100;

  // 희망구역 최대 개수
  public static final int HOPE_AREA_MAX_SIZE = 5;

  // 수정 가능한 신청서 상태
  public static final List<ApplicationFormStatus> EDITABLE_APPLICATION_FORM_STATUS = List.of(
      ApplicationFormStatus.CANCELED,
      ApplicationFormStatus.REJECTED,
      ApplicationFormStatus.CANCELED_IN_PROCESS
  );
}
