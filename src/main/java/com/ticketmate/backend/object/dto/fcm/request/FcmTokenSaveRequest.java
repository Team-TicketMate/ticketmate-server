package com.ticketmate.backend.object.dto.fcm.request;


import com.ticketmate.backend.object.constants.MemberPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FcmTokenSaveRequest {
    @NotBlank(message = "FCM 토큰을 입력해주세요")
    @Schema(defaultValue = "chM0thKFkDxdhMm5WAMMwQ:APA91bGKs8nN4A_bPClciFu88Z2bgN9-gvPKsopGsxPcKS2K86Gu9JcZi0HPHgcwpONymKpTiMPE4ztmIt0qXOl9gKUom13Aze80CkvPE6JwEuAgxJDRGtg")
    private String fmcToken; // FCM 토큰

    private MemberPlatform memberPlatform;
}
