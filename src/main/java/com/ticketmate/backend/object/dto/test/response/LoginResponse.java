package com.ticketmate.backend.object.dto.test.response;

import com.ticketmate.backend.object.constants.MemberType;
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
public class LoginResponse {

  private UUID memberId;
  private MemberType memberType;
  private String accessToken;
}
