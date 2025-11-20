package com.ticketmate.backend.fulfillmentform.infrastructure.repository;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentFormRepository extends JpaRepository<FulfillmentForm, UUID> {

  boolean existsByChatRoomIdAndApplicationForm(String chatRoomId, ApplicationForm applicationForm);
}
