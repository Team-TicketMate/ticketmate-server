package com.match.backend.common.application.exception;

import java.util.Map;
import lombok.Builder;

/**
 * 예시 응답값
 * "code" : "400"
 * "message" : "잘못된 요청입니다."
 * "title" : "값을 입력해주세요"
 */
@Builder
public record ValidErrorResponse(String errorCode, String errorMessage, Map<String, String> validation) {

  public void addValidation(String field, String message) {
    this.validation.put(field, message);
  }
}
