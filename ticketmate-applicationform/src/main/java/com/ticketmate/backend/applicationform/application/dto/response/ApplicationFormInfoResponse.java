package com.ticketmate.backend.applicationform.application.dto.response;

import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import java.util.List;
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
public class ApplicationFormInfoResponse {

  private ConcertInfoResponse concertInfoResponse;

  private List<ApplicationFormDetailResponse> applicationFormDetailResponseList;

}
