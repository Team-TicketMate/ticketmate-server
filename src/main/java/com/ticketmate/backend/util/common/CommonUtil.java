package com.ticketmate.backend.util.common;

import lombok.extern.slf4j.Slf4j;

/**
 * 공통 메서드
 */
@Slf4j
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
     * Enum 값을 String 으로 변환
     *
     * @param enumValue 변환한 Enum 값
     * @return Enum의 name() 또는 빈 문자열
     */
    public static String enumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : "";
    }
}
