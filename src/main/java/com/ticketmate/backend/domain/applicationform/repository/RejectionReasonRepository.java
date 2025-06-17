package com.ticketmate.backend.domain.applicationform.repository;

import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.RejectionReason;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectionReasonRepository extends JpaRepository<RejectionReason, UUID> {

  Optional<RejectionReason> findByApplicationForm(ApplicationForm applicationForm);
}
