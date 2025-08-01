package com.ticketmate.backend.common.application.exception;

import lombok.Builder;

@Builder
public record ErrorResponse(ErrorCode errorCode, String errorMessage) {

}
