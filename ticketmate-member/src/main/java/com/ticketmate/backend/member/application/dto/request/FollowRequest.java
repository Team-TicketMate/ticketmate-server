package com.ticketmate.backend.member.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
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
public class FollowRequest {

  @NotNull(message = "팔로우/언팔로우 대상 회원 PK를 입력하세요")
  private UUID followeeId; // 팔로우/언팔로우 대상자 pk
}
