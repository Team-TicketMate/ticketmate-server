package com.ticketmate.backend.member.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
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
public class MemberFollowRequest {

  @NotNull
  @NotNullErrorCode(ErrorCode.FOLLOWEE_ID_EMPTY)
  private UUID followeeId; // 팔로우/언팔로우 대상자 pk
}
