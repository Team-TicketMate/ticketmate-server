package com.ticketmate.backend.repository.postgres.expressions;

import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.object.postgres.application.RejectionReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpressionsRepository extends JpaRepository<RejectionReason, UUID> {
    Optional<RejectionReason> findByApplicationForm(ApplicationForm applicationForm);
}
