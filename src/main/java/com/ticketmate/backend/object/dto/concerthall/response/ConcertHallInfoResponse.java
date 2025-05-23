package com.ticketmate.backend.object.dto.concerthall.response;

import com.ticketmate.backend.object.constants.City;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertHallInfoResponse {

    private String ConcertName; // 공연장 명
    private String address; // 주소
    private City city; // 지역
    private String websiteUrl; // 사이트 URL
}
