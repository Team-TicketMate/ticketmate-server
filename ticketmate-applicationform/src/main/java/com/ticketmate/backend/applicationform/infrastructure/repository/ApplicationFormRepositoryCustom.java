package com.ticketmate.backend.applicationform.infrastructure.repository;

import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationFormRepositoryCustom {

  // 신청서 필터링 조회
  Page<ApplicationFormFilteredResponse> filteredApplicationForm(
      UUID clientId,
      UUID agentId,
      UUID concertId,
      Set<ApplicationFormStatus> applicationFormStatusSet,
      Pageable pageable
  );
}
