package com.ticketmate.backend.api.application.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.ErrorResponse;
import com.ticketmate.backend.common.application.exception.annotation.EmailErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotEmptyErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.PatternErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import java.lang.reflect.Field;
import java.security.Principal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
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
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    log.error("ValidationException 발생: {}", e.getMessage(), e);

    FieldError fieldError = e.getFieldError();
    ErrorCode errorCode = extractValidationErrorCode(e, fieldError);
    String errorMessage = errorCode.getMessage();

    ErrorResponse response = ErrorResponse.from(errorCode, errorMessage);

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  /**
   * 커스텀 예외 처리
   */
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    log.error("CustomException 발생: {}", e.getMessage(), e);

    ErrorCode errorCode = e.getErrorCode();
    String formattedMessage = formatMessage(errorCode.getMessage(), e.getArgs());

    ErrorResponse response = ErrorResponse.from(errorCode, formattedMessage);

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

  private String formatMessage(String message, Object[] args) {
    if (args == null || args.length == 0) {
      return message;
    }
    return new MessageFormat(message).format(args);
  }

  /**
   * FieldError에서 Validation 전용 ErrorCode 어노테이션을 추출하여 ErrorCode를 반환
   */
  private ErrorCode extractValidationErrorCode(MethodArgumentNotValidException exception, FieldError fieldError) {
    if (fieldError == null) {
      return ErrorCode.INVALID_REQUEST;
    }

    try {
      Object target = exception.getBindingResult().getTarget();
      if (target == null) {
        return ErrorCode.INVALID_REQUEST;
      }

      String fieldName = fieldError.getField();

      String validationCode = fieldError.getCode();
      if (CommonUtil.nvl(validationCode, "").isEmpty()) {
        return ErrorCode.INVALID_REQUEST;
      }

      Class<?> targetClass = target.getClass();
      Field field = targetClass.getDeclaredField(fieldName);

      // validation 타입에 따라 해당 ErrorCode 어노테이션 찾기
      return switch (validationCode) {
        case "NotBlank" -> {
          NotBlankErrorCode annotation = field.getAnnotation(NotBlankErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        case "NotNull" -> {
          NotNullErrorCode annotation = field.getAnnotation(NotNullErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        case "NotEmpty" -> {
          NotEmptyErrorCode annotation = field.getAnnotation(NotEmptyErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        case "Size" -> {
          SizeErrorCode annotation = field.getAnnotation(SizeErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        case "Min", "DecimalMin" -> {
          MinErrorCode annotation = field.getAnnotation(MinErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        case "Max", "DecimalMax" -> {
          MaxErrorCode annotation = field.getAnnotation(MaxErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        case "Pattern" -> {
          PatternErrorCode annotation = field.getAnnotation(PatternErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        case "Email" -> {
          EmailErrorCode annotation = field.getAnnotation(EmailErrorCode.class);
          yield annotation != null ? annotation.value() : ErrorCode.INVALID_REQUEST;
        }
        default -> {
          log.warn("처리되지 않은 validation 타입: {}", validationCode);
          yield ErrorCode.INVALID_REQUEST;
        }
      };

    } catch (NoSuchFieldException ex) {
      log.warn("필드를 찾을 수 없습니다: field={}", fieldError.getField(), ex);
    } catch (Exception ex) {
      log.warn("ErrorCode 추출 중 예외 발생: {}", ex.getMessage(), ex);
    }

    return ErrorCode.INVALID_REQUEST;
  }
}
