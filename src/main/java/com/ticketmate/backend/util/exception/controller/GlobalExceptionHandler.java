package com.ticketmate.backend.util.exception.controller;

import com.ticketmate.backend.object.ApiResponse;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 1) Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        // Validation 에러 정보를 담을 Map 생성
        Map<String, String> validation = new HashMap<>();
        for (FieldError fieldError : e.getFieldErrors()) {
            validation.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        // 공통 응답 DTO를 활용해 반환
        // ErrorCode.INVALID_REQUEST -> 400
        ApiResponse<?> response = ApiResponse.errorWithValidation(
                String.valueOf(HttpStatus.BAD_REQUEST.value()), // "400"
                "잘못된 요청입니다.",
                validation
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 2) 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {}", e.getMessage(), e);

        ErrorCode errorCode = e.getErrorCode();
        // 공통 응답 DTO를 활용
        ApiResponse<?> response = ApiResponse.error(
                String.valueOf(errorCode.getStatus().value()),  // 예: "400", "404" 등
                errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /**
     * 3) 그 외 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Unhandled Exception 발생: {}", e.getMessage(), e);

        // 예상치 못한 예외 => 500
        ApiResponse<?> response = ApiResponse.error(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "서버에 문제가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
