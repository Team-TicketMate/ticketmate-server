package com.ticketmate.backend.object.dto.chat.reqeust;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 사용자가 채팅방 검색시 사용될 DTO
 * 필터링 종류 = 키워드 검색, 선예매만 보기, 일반예매만 보기
 */
@AllArgsConstructor
@Getter
@Setter
public class ChatRoomFilteredRequest {
    public enum PreOpenFilter {
        ALL, PRE_OPEN, NORMAL
    }

    @Schema(defaultValue = "ALL")
    private PreOpenFilter isPreOpen;  // 선예매 일반예매 (Enum 사용으로 null 방지)
    @Min(value = 0, message = "페이지 번호는 최소 0부터 입니다.")
    private Integer pageNum;  // 페이지 번호
    @Schema(defaultValue = "")
    @Size(max = 30, message = "검색어는 최대 30자 입니다.")
    private String searchKeyword;  // 검색 키워드
}
