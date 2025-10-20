package com.ticketmate.backend.applicationform.application.dto.response;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.util.List;

public record ApplicationFormInfoResponse(
    ConcertInfoResponse concertInfoResponse,
    List<ApplicationFormDetailResponse> applicationFormDetailResponseList,
    ApplicationFormStatus applicationFormStatus,
    TicketOpenType ticketOpenType
) {

  public ApplicationFormInfoResponse {
    applicationFormDetailResponseList = CommonUtil.nullOrEmpty(applicationFormDetailResponseList)
        ? List.of()
        : List.copyOf(applicationFormDetailResponseList);
  }
}
