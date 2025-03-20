package com.ticketmate.backend.object.dto.concerthall.response;

import lombok.*;

import java.util.UUID;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertHallFilteredResponse {

    private UUID concertHallId;
    private String concertHallName;
}
