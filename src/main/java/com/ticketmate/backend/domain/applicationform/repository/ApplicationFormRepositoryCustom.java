package com.ticketmate.backend.domain.applicationform.repository;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
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
