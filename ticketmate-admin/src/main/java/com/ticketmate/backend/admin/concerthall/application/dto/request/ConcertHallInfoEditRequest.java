package com.ticketmate.backend.admin.concerthall.application.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertHallInfoEditRequest {

  private String concertHallName;

  private String address;

  @Pattern(regexp = "^(https://|http://).*$", message = "웹사이트 URL 형식이 올바르지 않습니다.")
  private String webSiteUrl;
}
