package com.ticketmate.backend.util.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public record ErrorDetail(
        String errorCode,
        String errorMessage,
        Map<String, String> validation) {

}
