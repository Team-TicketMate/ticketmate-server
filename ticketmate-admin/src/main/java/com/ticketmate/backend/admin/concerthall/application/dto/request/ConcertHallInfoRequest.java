package com.ticketmate.backend.admin.concerthall.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
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

  @NotBlank
  @NotBlankErrorCode(ErrorCode.CONCERT_HALL_NAME_EMPTY)
  private String concertHallName;

  private String address;

  private String webSiteUrl;
}
