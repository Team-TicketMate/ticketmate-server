package com.ticketmate.backend.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(defaultValue = "장충체육관")
  private String concertHallName;

  @Schema(defaultValue = "서울특별시 중구 동호로 241 (장충동2가)")
  private String address;

  @Pattern(regexp = "^(https://|http://).*$", message = "웹사이트 URL 형식이 올바르지 않습니다.")
  @Schema(defaultValue = "https://www.sisul.or.kr/open_content/jangchung/")
  private String webSiteUrl;
}
