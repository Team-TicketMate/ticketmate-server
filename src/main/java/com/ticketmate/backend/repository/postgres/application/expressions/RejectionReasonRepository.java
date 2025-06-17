package com.ticketmate.backend.repository.postgres.application.expressions;

import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.object.postgres.application.RejectionReason;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectionReasonRepository extends JpaRepository<RejectionReason, UUID> {

  Optional<RejectionReason> findByApplicationForm(ApplicationForm applicationForm);
}
