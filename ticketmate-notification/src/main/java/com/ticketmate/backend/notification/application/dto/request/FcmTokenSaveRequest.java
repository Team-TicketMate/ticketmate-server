package com.ticketmate.backend.notification.application.dto.request;

import com.ticketmate.backend.notification.core.constant.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FcmTokenSaveRequest {

  @NotBlank(message = "FCM 토큰을 입력해주세요")
  private String fcmToken; // FCM 토큰

  private DeviceType deviceType;
}
