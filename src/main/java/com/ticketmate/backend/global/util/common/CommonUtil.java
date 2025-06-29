package com.ticketmate.backend.global.util.common;

import java.util.List;

/**
 * 공통 메서드
 */
public class CommonUtil {

  /**
   * null 문자 처리 -> str1이 null 인 경우 str2 반환
   * "null" 문자열 처리 -> str1이 "null" 인 경우 str2 반환
   * str1이 빈 문자열 or 공백인 경우 -> str2 반환
   *
   * @param str1 검증할 문자열
   * @param str2 str1 이 null 인경우 반환할 문자열
   * @return null 이 아닌 문자열
   */
  public static String nvl(String str1, String str2) {
    if (str1 == null) { // str1 이 null 인 경우
      return str2;
    } else if (str1.equals("null")) { // str1 이 문자열 "null" 인 경우
      return str2;
    } else if (str1.isBlank()) { // str1 이 "" or " " 인 경우
      return str2;
    }
    return str1;
  }

  /**
   * Integer val 값이 null 인 경우 0으로 변환 후 반환
   *
   * @param val 검증할 Integer 래퍼클래스 정수 val
   * @return null 이 아닌 정수 값
   */
  public static int null2ZeroInt(Integer val) {
    if (val == null) { // val 이 null 인경우 0 반환
      return 0;
    }
    return val;
  }

  /**
   * 리스트가 null이거나 비어있는지 여부를 반환
   *
   * @param list 검증할 list
   * @return 리스트가 null이거나 비어있으면 true, 그 외에는 false
   */
  public static boolean nullOrEmpty(List<?> list) {
    return list == null || list.isEmpty();
  }

  /**
   * Enum 값을 String 으로 변환
   *
   * @param enumValue 변환한 Enum 값
   * @return Enum의 name() 또는 빈 문자열
   */
  public static String enumToString(Enum<?> enumValue) {
    return enumValue != null ? enumValue.name() : "";
  }

  /**
   * 특수문자 제거
   * 영숫자 (a-z, A-Z, 0-9)와 공백을 제외한 모든 값을 제거합니다
   */
  public static String removeSpecialCharacters(String input) {
    if (input == null) {
      return "";
    }
    // [^\\p{Alnum}\\s] : 영숫자와 공백이 아닌 모든 문자 매칭
    return input.replaceAll("[^\\p{Alnum}\\s]", "");
  }

  /**
   * 특수문자 변환
   * 영숫자 (a-z, A-Z, 0-9)와 공백을 제외한 모든 값을 원하는 값으로 변환합니다.
   */
  public static String replaceSpecialCharacters(String input, String replacement) {
    if (input == null) {
      return "";
    }
    // [^\\p{Alnum}\\s] : 영숫자와 공백이 아닌 모든 문자 매칭
    return input.replaceAll("[^\\p{Alnum}\\s]", replacement);
  }
}
