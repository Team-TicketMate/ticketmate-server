package com.ticketmate.backend.object.dto.application.response;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HopeAreaResponse {

    private Integer priority; // 우선 순위 (1~10)

    private String location; // 위치 (예: A구역, B구역)

    private Long price; // 가격
}
