package com.ticketmate.backend.repository.postgres.application;

import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, UUID> {
}
