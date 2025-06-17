package com.ticketmate.backend.global.exception;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 예시 응답값
 * "code" : "400"
 * "message" : "잘못된 요청입니다."
 * "title" : "값을 입력해주세요"
 */
@Getter
@Builder
@AllArgsConstructor
public class ValidErrorResponse {

  private final String errorCode;
  private final String errorMessage;
  private final Map<String, String> validation;

  public void addValidation(String field, String message) {
    this.validation.put(field, message);
  }
}
