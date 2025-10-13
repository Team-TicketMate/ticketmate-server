package com.ticketmate.backend.applicationform.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApplicationFormDetailResponse(
    LocalDateTime performanceDate, // 공연 일자
    Integer session, // 회차
    Integer requestCount, // 요청 매수
    List<HopeAreaResponse> hopeAreaResponseList, // 희망 구역 리스트
    String requirement // 요청 사항
) {

}
