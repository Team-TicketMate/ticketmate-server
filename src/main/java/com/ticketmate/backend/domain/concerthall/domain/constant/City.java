package com.ticketmate.backend.domain.concerthall.domain.constant;

import static com.ticketmate.backend.global.util.common.CommonUtil.nvl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum City {
  SEOUL(11, "서울특별시", "서울"),
  BUSAN(26, "부산광역시", "부산"),
  DAEGU(27, "대구광역시", "대구"),
  INCHEON(28, "인천광역시", "인천"),
  GWANGJU(29, "광주광역시", "광주"),
  DAEJEON(30, "대전광역시", "대전"),
  ULSAN(31, "울산광역시", "울산"),
  SEJONG(36, "세종특별자치시", "세종"),
  GYEONGGI(41, "경기도", "경기"),
  GANGWON(42, "강원도", "강원"),
  CHUNGCHEONG_BUK(43, "충청북도", "충북"),
  CHUNGCHEONG_NAM(44, "충청남도", "충남"),
  JEOLLA_BUK(45, "전라북도", "전북"),
  JEOLLA_NAM(46, "전라남도", "전남"),
  GYEONGSANG_BUK(47, "경상북도", "경북"),
  GYEONGSANG_NAM(48, "경상남도", "경남"),
  JEJU(50, "제주특별자치도", "제주");

  private final int cityCode;
  private final String fullName;
  private final String shortName;

  /**
   * 주소 문자열에서 시/도 이름을 기반으로 City를 반환
   *
   * @param address 주소
   * @return 매핑된 City, 없으면 null
   */
  public static City fromAddress(String address) {
    if (nvl(address, "").isEmpty()) {
      log.warn("요청된 주소가 null 입니다");
      return null;
    }

    for (City city : City.values()) {
      if (address.contains(city.getFullName()) || address.contains(city.getShortName())) {
        log.debug("입력된 주소에 해당하는 City: 지역={} 코드={}", city, city.getCityCode());
        return city;
      }
    }
    log.warn("입력된 주소에 해당하는 City를 찾을 수 없습니다.");
    return null;
  }

  /**
   * 지역 코드와 일치하는 City 반환
   *
   * @param cityCode 지역코드
   * @return 매핑된 City, 없으면 null
   */
  public static City fromCityCode(int cityCode) {
    for (City city : City.values()) {
      if (city.getCityCode() == cityCode) {
        log.debug("입력한 지역코드에 해당하는 city: 지역={} 코드{}", city, city.getCityCode());
        return city;
      }
    }
    log.warn("입력한 지역 코드에 해당하는 City 찾을 수 없습니다.");
    return null;
  }
}
