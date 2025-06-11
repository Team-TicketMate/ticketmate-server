package com.ticketmate.backend.object.dto.test.response;

import com.ticketmate.backend.object.constants.MemberType;
import lombok.*;

import java.util.UUID;

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
