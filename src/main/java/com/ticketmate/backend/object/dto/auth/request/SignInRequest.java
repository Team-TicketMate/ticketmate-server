package com.ticketmate.backend.object.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SignInRequest {

    @NotBlank(message = "로그인 이메일을 입력하세요")
    @Schema(defaultValue = "example@naver.com")
    private String username;

    @NotBlank(message = "로그인 비밀번호를 입력하세요")
    @Schema(defaultValue = "pw12345")
    private String password;
}
