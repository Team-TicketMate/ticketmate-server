package com.ticketmate.backend.fulfillmentform.application.mapper.successhistory;


import com.ticketmate.backend.fulfillmentform.application.dto.successhistory.response.SuccessHistoryResponse;
import com.ticketmate.backend.fulfillmentform.infrastructure.repository.successhistory.SuccessHistoryRow;

public interface SuccessHistoryMapper {

  SuccessHistoryResponse toSuccessHistoryResponse(SuccessHistoryRow successHistoryRow);
}
