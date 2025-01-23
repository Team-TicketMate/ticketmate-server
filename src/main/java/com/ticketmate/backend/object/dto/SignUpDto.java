package com.ticketmate.backend.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SignUpDto {

    @Schema(defaultValue = "example@naver.com")
    private String username; // 이메일

    @Schema(defaultValue = "pw12345")
    private String password; // 비밀번호

    @Schema(defaultValue = "nickname123")
    private String nickname; // 닉네임

    @Schema(defaultValue = "19980114")
    private String birth; // 생년월일 (ex.19980114)

    @Schema(defaultValue = "01012345678")
    private String phone; // 전화번호
}
