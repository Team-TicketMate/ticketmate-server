package com.ticketmate.backend.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SignUpRequest {

    @NotBlank(message = "이메일을 입력하세요")
    @Email(message = "이메일 형식이 아닙니다")
    @Schema(defaultValue = "example@naver.com")
    private String username; // 이메일

    @NotBlank(message = "비밀번호를 입력하세요")
    @Schema(defaultValue = "pw12345")
    private String password; // 비밀번호

    @NotBlank(message = "닉네임을 입력하세요")
    @Schema(defaultValue = "nickname123")
    private String nickname; // 닉네임

    @NotBlank(message = "생년월일을 입력하세요")
    @Schema(defaultValue = "19980114")
    @Pattern(regexp = "^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$", message = "생년월일 형식은 YYYYMMDD 입니다")
    private String birth; // 생년월일 (ex.19980114)

    @NotBlank(message = "전화번호를 입력하세요")
    @Schema(defaultValue = "01012345678")
    @Pattern(regexp = "^01[0-9][0-9]{8}$", message = "전화번호 형식은 01012345678 입니다")
    private String phone; // 전화번호
}
