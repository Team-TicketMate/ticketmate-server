package com.ticketmate.backend.applicationform.infrastructure.repository;

import com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectionReasonRepository extends JpaRepository<RejectionReason, UUID> {

  Optional<RejectionReason> findByApplicationFormApplicationFormId(UUID applicationFormId);
}
