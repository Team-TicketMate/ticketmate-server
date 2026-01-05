package com.ticketmate.backend.fulfillmentform.application.mapper.fulfillmentform;

import com.ticketmate.backend.fulfillmentform.application.dto.fulfillmentform.response.FulfillmentFormInfoResponse;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;

public interface FulfillmentFormMapper {

  // FulfillmentForm -> FulfillmentFormInfoResponse (DTO)
  FulfillmentFormInfoResponse toFulfillmentFormResponse(FulfillmentForm fulfillmentForm);

}
