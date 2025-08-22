package com.ticketmate.backend.admin.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
public class ConcertDateRequest {

  @NotNull(message = "공연일자를 입력해주세요")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime performanceDate;

  @Min(value = 1, message = "최대 예매 매수는 1 이상이여야 합니다")
  @Max(value = Integer.MAX_VALUE, message = "최대 예매 매수는 정수 최대 범위를 넘을 수 없습니다.")
  @NotNull(message = "공연 회차를 입력해주세요")
  private Integer session;

  public ConcertDate toEntity(Concert concert) {
    return ConcertDate.builder()
        .concert(concert)
        .performanceDate(performanceDate)
        .session(session)
        .build();
  }
}
