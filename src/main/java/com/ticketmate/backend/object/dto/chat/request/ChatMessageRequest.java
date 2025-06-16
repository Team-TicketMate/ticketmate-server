package com.ticketmate.backend.object.dto.chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "메시지를 입력해주세요.")
    @Size(max = 500, message = "메시지는 500자를 초과할 수 없습니다")
    private String message;
}
