package com.ticketmate.backend.applicationform.infrastructure.repository;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationFormDetail;
import java.util.List;
import java.util.UUID;

public interface ApplicationFormDetailRepositoryCustom {

  // ApplicationFormId로 ApplicationFormDetail과 관련된 하위 엔티티들을 한 번에 조회
  List<ApplicationFormDetail> findAllApplicationFormDetailWithHopeAreaListByApplicationFormId(UUID applicationFormId);

}
