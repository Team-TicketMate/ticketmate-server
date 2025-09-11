package com.ticketmate.backend.admin.concerthall.application.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class ConcertHallInfoRequest {

  @NotBlank(message = "공연장 명을 입력해주세요")
  private String concertHallName;

  private String address;

  private String webSiteUrl;
}
