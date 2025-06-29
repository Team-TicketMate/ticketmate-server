package com.ticketmate.backend.domain.notification.domain.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
