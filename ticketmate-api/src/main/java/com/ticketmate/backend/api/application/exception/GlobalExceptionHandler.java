package com.ticketmate.backend.api.application.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.ErrorResponse;
import com.ticketmate.backend.common.application.exception.ValidErrorResponse;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private static final String ERROR_DESTINATION = "/queue/errors";
  private final SimpMessagingTemplate template;

  /**
   * Validation 예외 처리
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    log.error("ValidationException 발생: {}", e.getMessage(), e);
    // Validation 에러 정보를 담을 Map 생성
    Map<String, String> validation = new HashMap<>();
    for (FieldError fieldError : e.getFieldErrors()) {
      validation.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    // 공통 응답 DTO를 활용해 반환
    // ErrorCode.INVALID_REQUEST -> 400
    ValidErrorResponse response = ValidErrorResponse.builder()
        .errorCode(HttpStatus.BAD_REQUEST.toString())
        .errorMessage("잘못된 요청입니다.")
        .validation(validation)
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * 커스텀 예외 처리
   */
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    log.error("CustomException 발생: {}", e.getMessage(), e);

    ErrorCode errorCode = e.getErrorCode();

    ErrorResponse response = ErrorResponse.from(errorCode, errorCode.getMessage());

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  /**
   * WebSocket 통신 중 발생한 예외(비즈니스 로직)처리
   * StompExceptionInterceptor 내부에서 처리를 하지 못하는 상활일 때의 예외를 핸들링합니다.
   */
  @MessageExceptionHandler(CustomException.class)
  public void handleRuntimeException(Principal principal, CustomException e, @Payload Message<byte[]> message, StompHeaderAccessor accessor) {
    log.error("WebSocket 통신 중 CustomException 발생: {}", e.getMessage());

    ErrorCode errorCode = e.getErrorCode();

    ErrorResponse response = ErrorResponse.from(errorCode, errorCode.getMessage());

    template.convertAndSendToUser(principal.getName(), ERROR_DESTINATION, response);
  }

  /**
   * JSON 바디 파싱 실패 (ex. LocalDateTime 포맷 불일치 등)
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    log.error("HttpMessageNotReadableException 발생: {}", e.getMessage());

    if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
      // LocalDateTime 포맷 오류
      if (invalidFormatException.getTargetType() != null && LocalDateTime.class.isAssignableFrom(invalidFormatException.getTargetType())) {
        ErrorCode errorCode = ErrorCode.INVALID_DATE_TIME_PARSE;
        ErrorResponse response = ErrorResponse.from(errorCode, errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
      }
    }
    // 그 외 JSON 파싱 오류는 400 응답
    ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
    ErrorResponse response = ErrorResponse.from(errorCode, errorCode.getMessage());
    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  /**
   * 그 외 예외 처리
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled Exception 발생: {}", e.getMessage(), e);

    // 예상치 못한 에러 => 500
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    ErrorResponse response = ErrorResponse.from(errorCode, errorCode.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
