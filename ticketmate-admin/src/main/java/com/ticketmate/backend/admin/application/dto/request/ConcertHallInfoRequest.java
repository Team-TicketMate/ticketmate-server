package com.ticketmate.backend.admin.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertHallInfoRequest {

  @NotBlank(message = "공연장 명을 입력해주세요")
  private String concertHallName;

  private String address;

  @Pattern(regexp = "^(https://|http://).*$", message = "웹사이트 URL 형식이 올바르지 않습니다.")
  private String webSiteUrl;
}
