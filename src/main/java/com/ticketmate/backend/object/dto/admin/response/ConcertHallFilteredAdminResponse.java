package com.ticketmate.backend.object.dto.admin.response;

import com.ticketmate.backend.object.constants.City;
import lombok.*;

import java.util.UUID;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertHallFilteredAdminResponse {

    private UUID concertHallId;
    private String concertHallName;
    private String address;
    private City city;
    private String webSiteUrl;
}
