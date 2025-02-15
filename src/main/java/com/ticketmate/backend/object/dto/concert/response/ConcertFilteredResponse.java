package com.ticketmate.backend.object.dto.concert.response;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertFilteredResponse {
    private String concertName;
    private String concertHallName;
    private ConcertType concertType;
    private LocalDateTime ticketPreOpenDate;
    private LocalDateTime ticketOpenDate;
    private Integer duration;
    private Integer session;
    private String concertThumbnailUrl;
    private TicketReservationSite ticketReservationSite;
}
