package com.ticketmate.backend.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum City {
    SEOUL("서울특별시"),
    INCHEON("인천광역시"),
    GYEONGGI("경기도"),
    DAEJEON("대전광역시"),
    GANGWON("강원도"),
    BUSAN("부산광역시"),
    JEJU("제주특별자치도"),
    DAEGU("대구광역시"),
    GWANGJU("광주광역시"),
    ULSAN("울산광역시"),
    SEJONG("세종특별자치시"),
    CHUNGCHEONGNAM("충청남도"),
    CHUNGCHEONGBUK("충청북도"),
    JEOLLANAM("전라남도"),
    JEOLLABUK("전라북도"),
    GYEONGSANGNAM("경상남도"),
    GYEONGSANGBUK("경상북도");

    private final String description;
}
