package com.ticketmate.backend.auth.application.dto.response;

public record LoginResponse(
    boolean totpEnabled,
    String preAuthToken
) {

}
