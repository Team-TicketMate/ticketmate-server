package com.ticketmate.backend.mock.application.dto.response;

import com.ticketmate.backend.member.core.constant.MemberType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MockLoginResponse {

  private UUID memberId;
  private MemberType memberType;
  private String accessToken;
}
