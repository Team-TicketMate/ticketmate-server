package com.ticketmate.backend.applicationform.application.dto.response;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
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

  private LocalDateTime submittedDate; // 신청 일자

  private ApplicationFormStatus applicationFormStatus; // 신청서 상태

  private TicketOpenType ticketOpenType; // 선예매/일반예매 타입
}
