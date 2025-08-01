package com.ticketmate.backend.applicationform.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime performanceDate; // 공연 일자

  private Integer session; // 회차

  private Integer requestCount; // 요청 매수

  private List<HopeAreaResponse> hopeAreaResponseList; // 희망 구역 리스트

  private String requirement; // 요청 사항
}
