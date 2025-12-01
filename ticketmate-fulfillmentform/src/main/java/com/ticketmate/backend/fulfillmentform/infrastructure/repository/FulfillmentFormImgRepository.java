package com.ticketmate.backend.fulfillmentform.infrastructure.repository;

import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentFormImg;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentFormImgRepository extends JpaRepository<FulfillmentFormImg, UUID> {
}
