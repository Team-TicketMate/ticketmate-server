package com.ticketmate.backend.domain.applicationform.domain.dto.request;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormRequest {

  private UUID agentId; // 대리인 PK

  private UUID concertId; // 공연 PK

  @NotEmpty(message = "신청서에는 최소 1개 이상의 신청서 세부사항이 포함되어야 합니다")
  private List<ApplicationFormDetailRequest> applicationFormDetailRequestList; // 신청서 세부사항 List

  @NotNull(message = "선예매/일반예매 타입을 입력해주세요")
  private TicketOpenType ticketOpenType; // 선예매 여부
}
