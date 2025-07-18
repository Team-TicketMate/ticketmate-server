package com.ticketmate.backend.domain.chat.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatMessageType {
    TEXT("텍스트"),

    PICTURE("이미지");

    private final String description;
}
