package com.ticketmate.backend.domain.applicationform.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormFilteredResponse {

  private UUID applicationFormId; // 신청서 PK
  private String concertName; // 공연 제목
  private String concertThumbnailUrl; // 공연 썸네일 URL
  private String agentNickname; // 대리인 닉네임
  private String clientNickname; // 의뢰인 닉네임
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime submittedDate; // 신청 일자
  private ApplicationFormStatus applicationFormStatus; // 신청서 상태
  private TicketOpenType ticketOpenType; // 선예매/일반예매 타입
}
