package com.ticketmate.backend.chat.infrastructure.constant;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class ChatConstants {
    public static final String LAST_READ_MESSAGE_POINTER_KEY = "userLastRead:%s:%s";
    public static final String UN_READ_MESSAGE_COUNTER_KEY = "unRead:%s:%s";
    public static final Duration TTL = Duration.ofDays(30);
    public static final DateTimeFormatter ISO_SEC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final String MESSAGE_PICTURE_PREVIEW_FORMAT = "사진을 보냈습니다.";
    public static final int CHAT_PICTURE_MAX_SIZE = 10;
}
