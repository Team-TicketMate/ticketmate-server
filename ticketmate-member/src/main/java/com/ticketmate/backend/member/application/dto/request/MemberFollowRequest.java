package com.ticketmate.backend.member.application.dto.request;

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

  @NotNull(message = "팔로우/언팔로우 대상 회원 PK를 입력하세요")
  private UUID followeeId; // 팔로우/언팔로우 대상자 pk
}
