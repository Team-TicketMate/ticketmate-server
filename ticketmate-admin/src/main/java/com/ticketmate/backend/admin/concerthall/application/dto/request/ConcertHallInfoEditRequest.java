package com.ticketmate.backend.admin.concerthall.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.PatternErrorCode;
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

  @Pattern(regexp = "^(https://|http://).*$")
  @PatternErrorCode(ErrorCode.WEB_SITE_URL_PATTERN_INVALID)
  private String webSiteUrl;
}
