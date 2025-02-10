package com.ticketmate.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

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
     * Entity 객체를 지정된 DTO 타입으로 변환합니다.
     *
     * @param entity   변환할 Entity 객체 (null이면 null 반환)
     * @param dtoClass DTO 클래스 타입
     * @param <D>      DTO 타입
     * @param <E>      Entity 타입
     * @return Entity의 프로퍼티를 복사한 DTO 객체
     */
    public static <D, E> D convertEntityToDto(E entity, Class<D> dtoClass) {
        if (entity == null) {
            return null;
        }
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        } catch (Exception e) {
            log.error("Entity를 DTO로 변환하는 중 오류가 발생했습니다.", e);
            throw new RuntimeException("Entity를 DTO로 변환하는 중 오류가 발생했습니다.", e);
        }
    }
}
