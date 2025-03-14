package com.ticketmate.backend.object.dto.notification.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class NotificationPayloadRequest {
    private String title;
    private String body;

    @Builder
    public NotificationPayloadRequest(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
