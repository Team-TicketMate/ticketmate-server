package com.ticketmate.backend.applicationform.infrastructure.constant;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationFormConstants {

  // 수정 가능한 신청서 상태
  public static final Set<ApplicationFormStatus> EDITABLE_APPLICATION_FORM_STATUS = Set.of(
    ApplicationFormStatus.CANCELED,
    ApplicationFormStatus.REJECTED,
    ApplicationFormStatus.CANCELED_IN_PROCESS
  );

  // 중복 검증을 진행할 신청서 상태
  public static final Set<ApplicationFormStatus> DUPLICATE_CHECK_APPLICATION_FORM_STATUS = Set.of(
    ApplicationFormStatus.PENDING,
    ApplicationFormStatus.APPROVED
  );
}
