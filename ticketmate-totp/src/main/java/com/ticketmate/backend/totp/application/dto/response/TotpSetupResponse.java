package com.ticketmate.backend.totp.application.dto.response;

import lombok.Builder;

@Builder
public record TotpSetupResponse(
    String secret,
    String otpAuthUrl
) {

}
