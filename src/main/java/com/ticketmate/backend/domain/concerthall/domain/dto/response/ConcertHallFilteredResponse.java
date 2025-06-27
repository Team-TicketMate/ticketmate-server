package com.ticketmate.backend.domain.concerthall.domain.dto.response;

import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import java.util.UUID;
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
public class ConcertHallFilteredResponse {

  private UUID concertHallId;
  private String concertHallName;
  private String address;
  private City city;
  private String webSiteUrl;
}
