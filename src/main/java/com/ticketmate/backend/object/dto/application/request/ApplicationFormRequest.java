package com.ticketmate.backend.object.dto.application.request;

import com.ticketmate.backend.object.constants.TicketOpenType;
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

  @NotNull(message = "대리인 PK 값을 입력해주세요")
  private UUID agentId; // 대리인 PK

  @NotNull(message = "콘서트 PK 값을 입력해주세요")
  private UUID concertId; // 콘서트 PK

  @NotEmpty(message = "공연일자 JSON을 입력하세요")
  private List<ApplicationFormDetailRequest> applicationFormDetailRequestList; // 신청서 공연회차 List

  @NotNull(message = "선예매/일반예매 타입을 입력해주세요")
  private TicketOpenType ticketOpenType; // 선예매 여부
}
