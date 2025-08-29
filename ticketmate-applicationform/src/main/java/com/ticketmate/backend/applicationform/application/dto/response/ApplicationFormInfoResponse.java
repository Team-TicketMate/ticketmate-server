package com.ticketmate.backend.applicationform.application.dto.response;

import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import java.util.List;

public record ApplicationFormInfoResponse(
    ConcertInfoResponse concertInfoResponse,
    List<ApplicationFormDetailResponse> applicationFormDetailResponseList
) {

}
