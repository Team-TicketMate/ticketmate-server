package com.ticketmate.backend.object.dto.application.response;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HopeAreaResponse {

    private Integer priority; // 순위

    private String location; // 위치

    private Long price; // 가격
}
