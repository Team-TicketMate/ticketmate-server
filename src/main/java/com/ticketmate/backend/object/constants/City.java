package com.ticketmate.backend.object.constants;

import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum City {
    SEOUL("서울특별시", "서울"),
    INCHEON("인천광역시", "인천"),
    GYEONGGI("경기도", "경기"),
    DAEJEON("대전광역시", "대전"),
    GANGWON("강원도", "강원"),
    BUSAN("부산광역시", "부산"),
    JEJU("제주특별자치도", "제주"),
    DAEGU("대구광역시", "대구"),
    GWANGJU("광주광역시", "광주"),
    ULSAN("울산광역시", "울산"),
    SEJONG("세종특별자치시", "세종"),
    CHUNGCHEONGNAM("충청남도", "충남"),
    CHUNGCHEONGBUK("충청북도", "충북"),
    JEOLLANAM("전라남도", "전남"),
    JEOLLABUK("전라북도", "전북"),
    GYEONGSANGNAM("경상남도", "경남"),
    GYEONGSANGBUK("경상북도", "경북");

    private final String description;
    private final String shortName;

    /**
     * 주소에 해당하는 city를 반환합니다.
     *
     * @param address 주소
     */
    public static City determineCityFromAddress(String address) {
        for (City city : City.values()) {
            if (address.contains(city.getDescription()) || address.contains(city.getShortName())) {
                log.debug("입력된 주소에 해당하는 city: {}", city.getDescription());
                return city;
            }
        }
        log.error("입력된 주소에 일치하는 city를 찾을 수 없습니다.");
        throw new CustomException(ErrorCode.CITY_NOT_FOUND);
    }
}
