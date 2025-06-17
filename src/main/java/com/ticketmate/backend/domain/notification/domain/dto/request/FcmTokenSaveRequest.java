package com.ticketmate.backend.domain.notification.domain.dto.request;


import com.ticketmate.backend.domain.notification.domain.constant.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(defaultValue = "chM0thKFkDxdhMm5WAMMwQ:APA91bGKs8nN4A_bPClciFu88Z2bgN9-gvPKsopGsxPcKS2K86Gu9JcZi0HPHgcwpONymKpTiMPE4ztmIt0qXOl9gKUom13Aze80CkvPE6JwEuAgxJDRGtg")
  private String fcmToken; // FCM 토큰

  private DeviceType deviceType;
}
