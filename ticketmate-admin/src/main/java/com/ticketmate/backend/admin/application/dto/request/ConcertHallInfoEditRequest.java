package com.ticketmate.backend.admin.application.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertHallInfoEditRequest {

  private String concertHallName;

  private String address;

  @Pattern(regexp = "^(https://|http://).*$", message = "웹사이트 URL 형식이 올바르지 않습니다.")
  private String webSiteUrl;
}
