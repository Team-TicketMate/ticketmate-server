package com.ticketmate.backend.object.dto;

import com.ticketmate.backend.util.exception.ErrorDetail;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Builder
@Getter
@ToString
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail errorDetail;

    /**
     * 요청이 성공했을 때
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .errorDetail(null)
                .build();
    }

    /**
     * 요청이 실패했을 때
     */
    public static <T> ApiResponse<T> error(String errorCode, String errorMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .errorDetail(ErrorDetail.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())
                .build();
    }

    /**
     * 요청 실패 + Validation 상세 오류 전달
     */
    public static <T> ApiResponse<T> errorWithValidation(
            String errorCode,
            String errorMessage,
            Map<String, String> validation) {

        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .errorDetail(ErrorDetail.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .validation(validation)
                        .build())
                .build();
    }
}
