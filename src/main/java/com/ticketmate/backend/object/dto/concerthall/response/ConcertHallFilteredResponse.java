package com.ticketmate.backend.object.dto.concerthall.response;

import com.ticketmate.backend.object.constants.City;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertHallFilteredResponse {

    private String concertHallName;

    private int capacity;

    private String address;

    private City city;

    private String concertHallUrl;
}
