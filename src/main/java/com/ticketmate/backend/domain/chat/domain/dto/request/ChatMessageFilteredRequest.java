package com.ticketmate.backend.domain.chat.domain.dto.request;

import com.ticketmate.backend.global.constant.PageableConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessageFilteredRequest {
    @Schema(defaultValue = "1", nullable = true)
    @Min(value = 1, message = "페이지 번호는 1이상 값을 입력해야합니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
    private Integer pageNumber; // 페이지 번호 (1부터 시작)

    @Schema(defaultValue = "20", nullable = true)
    @Min(value = 20, message = "체팅메시지당 데이터 최솟값은 20개 입니다.")
    @Max(value = PageableConstants.MAX_PAGE_SIZE, message = "체팅메시지당 데이터 최댓값은 " + PageableConstants.MAX_PAGE_SIZE + "개 입니다.")
    private Integer pageSize; // 페이지 사이즈

    public ChatMessageFilteredRequest() {
        this.pageNumber = 1;
        this.pageSize = PageableConstants.CHAT_MESSAGE_DEFAULT_PAGE_SIZE;
    }
}
