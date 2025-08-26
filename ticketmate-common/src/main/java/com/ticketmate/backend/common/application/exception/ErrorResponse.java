package com.ticketmate.backend.common.application.exception;

public record ErrorResponse(
    ErrorCode errorCode,
    String errorMessage
) {

  public static ErrorResponse from(ErrorCode errorCode, String errorMessage) {
    return new ErrorResponse(errorCode, errorMessage);
  }
}
