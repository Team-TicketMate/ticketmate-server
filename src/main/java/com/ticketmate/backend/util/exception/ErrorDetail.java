package com.ticketmate.backend.util.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class ErrorDetail {
    private final String errorCode;
    private final String errorMessage;
    private final Map<String, String> validation;
}
