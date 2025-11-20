package com.ticketmate.backend.fulfillmentform.application.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FulfillmentFormImgResponse {

  private UUID fulfillmentFormImgId;

  private String fulfillmentFormImgUrl;
}
