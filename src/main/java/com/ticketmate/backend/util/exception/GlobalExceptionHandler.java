package com.ticketmate.backend.util.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // 커스텀 예외 처리
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    log.error("{}", e.getMessage());
    log.error("stacktrace", e);
    ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());
    return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    log.error("{}", e.getMessage());
    log.error("stacktrace", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }
}
