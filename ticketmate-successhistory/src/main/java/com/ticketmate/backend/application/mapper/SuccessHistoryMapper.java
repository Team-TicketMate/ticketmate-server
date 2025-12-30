package com.ticketmate.backend.application.mapper;

import com.ticketmate.backend.application.dto.response.SuccessHistoryResponse;
import com.ticketmate.backend.infrastructure.repository.SuccessHistoryRow;

public interface SuccessHistoryMapper {

  SuccessHistoryResponse toSuccessHistoryResponse(SuccessHistoryRow successHistoryRow);
}
