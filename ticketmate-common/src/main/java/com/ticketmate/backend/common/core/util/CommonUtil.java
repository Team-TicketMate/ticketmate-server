package com.ticketmate.backend.common.core.util;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.constant.SortField;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 공통 메서드
 */
public class CommonUtil {

  // 모든 유니코드 글자 + 숫자 + 공백 허용
  private static final Pattern SPECIAL_CHARS = Pattern.compile("[^\\p{L}\\p{N}\\s]");

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
   * enumClass의 값 중 value와 매칭된 상수를 반환합니다
   *
   * @param <E>       enum 타입 (Enum<E>이면서 SortField 구현)
   * @param enumClass 해당 enum 클래스
   * @param value     클라이언트에서 보낸 문자열
   * @return 매칭된 enum 상수
   */
  public static <E extends Enum<E> & SortField> E stringToEnum(Class<E> enumClass, String value) {
    if (nvl(value, "").isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_SORT_FIELD);
    }

    return Arrays.stream(enumClass.getEnumConstants())
        .filter(e ->
            e.name().equalsIgnoreCase(value) ||
            e.getProperty().equals(value)
        )
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SORT_FIELD));
  }

  /**
   * 특수문자 제거
   * 영숫자 (a-z, A-Z, 0-9)와 공백을 제외한 모든 값을 제거합니다
   */
  public static String normalizeAndRemoveSpecialCharacters(String input) {
    return normalize(input, "");
  }

  /**
   * 특수문자 변환
   * 영숫자 (a-z, A-Z, 0-9)와 공백을 제외한 모든 값을 원하는 값으로 변환합니다.
   */
  public static String normalizeAndReplaceSpecialCharacters(String input, String replacement) {
    return normalize(input, replacement);
  }

  /**
   * Unicode 정규화
   * 텍스트 내 모든 특수문자 (문자(letter), 숫자(number), 공백 제외) 제거/치환
   * 연속 공백 -> 단일 공백
   * trim()
   *
   * @param input              정규화 할 문자열
   * @param specialReplacement 특수문자를 치환할 문자열 (제거 시 "" 입력, 치환 시 원하는 문자열 입력)
   * @return 정규화 된 문자열
   */
  private static String normalize(String input, String specialReplacement) {
    return Optional.ofNullable(input)
        .filter(s -> !s.isBlank())
        .map(s -> Normalizer.normalize(s, Form.NFKC)) // Unicode 정규화
        .map(s -> SPECIAL_CHARS.matcher(s).replaceAll(specialReplacement)) // 특수문자 제거/치환
        .map(s -> s.replaceAll("\\s+", " ").trim()) // 공백 정리 & trim
        .orElse("");
  }
}
