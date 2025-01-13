package com.ticketmate.backend.util.exception.controller;

import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorResponse;
import com.ticketmate.backend.util.exception.ValidErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // Validation 예외 처리
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseBody
  public ValidErrorResponse invalidRequestException(MethodArgumentNotValidException e) {
    ValidErrorResponse response = ValidErrorResponse.builder()
            .code("400")
            .message("잘못된 요청입니다.")
            .build();

    for (FieldError fieldError : e.getFieldErrors()) {
      response.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return response;
  }

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
