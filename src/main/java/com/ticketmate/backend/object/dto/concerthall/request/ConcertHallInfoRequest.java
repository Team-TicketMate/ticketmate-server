package com.ticketmate.backend.object.dto.concerthall.request;

import com.ticketmate.backend.object.postgres.Member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertHallInfoRequest {

    private Member member;

    @NotBlank(message = "공연장 이름을 입력하세요")
    @Schema(defaultValue = "인스파이어 아레나")
    private String concertHallName;

    @NotNull(message = "수용인원을 입력하세요")
    @Min(value = 1, message = "수용인원은 1명 이상이여야 합니다")
    @Schema(defaultValue = "15000")
    private Integer capacity;

    @NotBlank(message = "공연장 주소를 입력하세요")
    @Schema(defaultValue = "인천광역시 중구 공항문화로 127 인스파이어 아레나 (운서동 2955-74)")
    private String address;

    @NotBlank(message = "공연장 웹사이트 URL을 입력하세요")
    @Schema(defaultValue = "https://www.inspirekorea.com/ko/entertainment/inspire-arena/inspire-arena")
    @Pattern(regexp = "^(http://|https://).*", message = "웹사이트 URL 형식은 'http://' 또는 'https://' 로 시작해야합니다")
    private String concertHallUrl;
}
