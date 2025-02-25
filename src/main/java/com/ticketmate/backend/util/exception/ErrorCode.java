package com.ticketmate.backend.util.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // GLOBAL

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

    // AUTH

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    MISSING_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다."),

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 엑세스 토큰입니다."),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),

    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),

    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다."),

    // OAUTH2

    INVALID_SOCIAL_PLATFORM(HttpStatus.BAD_REQUEST, "잘못된 소셜 플랫폼이 요청되었습니다."),

    // MEMBER,

    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다"),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."),

    // CONCERT

    DUPLICATE_CONCERT_NAME(HttpStatus.BAD_REQUEST, "중복된 공연 제목입니다."),

    INVALID_RANGE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 범위가 입력되었습니다."),

    // CONCERT_HALL

    CONCERT_HALL_NAME_NOT_FOUND(HttpStatus.NOT_FOUND, "일치하는 공연장 정보를 찾을 수 없습니다."),

    DUPLICATE_CONCERT_HALL_NAME(HttpStatus.BAD_REQUEST, "중복된 공연장 이름입니다."),

    CITY_NOT_FOUND(HttpStatus.BAD_REQUEST, "주소에 일치하는 city값이 없습니다."),

    // FILE IO

    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),

    // S3

    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드 중 오류가 발생했습니다."),

    INVALID_INPUT_IMG(HttpStatus.BAD_REQUEST, "파일의 이름이 잘못되었습니다."),

    INVALID_IMG_FORMAT(HttpStatus.BAD_REQUEST, "이미지의 확장자가 잘못되었습니다. 확장자는 jpg, jpeg, png, JPG, JPEG, PNG 만 가능합니다"),


    // PORTFOLIO

    PORTFOLIO_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 포트폴리오를 찾을 수 없습니다."),

    PORTFOLIO_IMG_BLACK(HttpStatus.BAD_REQUEST, "자신을 소개할 수 있는 포트폴리오 이미지를 첨부해주세요."),

    PORTFOLIO_IMG_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "포트폴리오 등록을 위한 이미지는 최대 20장입니다.");

    private final HttpStatus status;
    private final String message;
}
