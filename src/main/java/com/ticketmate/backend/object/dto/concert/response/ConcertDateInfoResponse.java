package com.ticketmate.backend.object.dto.concert.response;

import lombok.*;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertDateInfoResponse {
    private LocalDateTime performanceDate; // 공연일자
    private Integer session; // 회차
}
