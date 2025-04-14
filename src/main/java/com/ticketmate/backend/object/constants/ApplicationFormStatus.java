package com.ticketmate.backend.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationFormStatus {
    PENDING("대기"),

    APPROVED("승인"),

    REJECTED("거절"),

    EXPIRED("만료");

    private final String description;
}
