package com.ticketmate.backend.concerthall.application.dto.response;

import com.ticketmate.backend.concerthall.core.constant.City;
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
public class ConcertHallInfoResponse {

  private String ConcertName; // 공연장 명
  private String address; // 주소
  private City city; // 지역
  private String websiteUrl; // 사이트 URL
}
