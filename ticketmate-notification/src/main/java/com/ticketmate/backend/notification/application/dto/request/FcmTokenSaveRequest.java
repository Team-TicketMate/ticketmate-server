package com.ticketmate.backend.notification.application.dto.request;

import com.ticketmate.backend.notification.core.constant.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenSaveRequest {

  @NotBlank(message = "fcmToken이 비어있습니다")
  private String fcmToken; // FCM 토큰

  private DeviceType deviceType;
}
