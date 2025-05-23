package com.ticketmate.backend.object.dto.application.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormDetailResponse {

    private LocalDateTime performanceDate; // 공연 일자

    private Integer session; // 회차

    private Integer requestCount; // 요청 매수

    private List<HopeAreaResponse> hopeAreaResponseList; // 희망 구역 리스트

    private String requirement; // 요청 사항
}
