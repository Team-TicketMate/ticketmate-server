package com.ticketmate.backend.auth.core.dto;

public record TokenPair(
    String accessToken,
    String refreshToken
) {

}
