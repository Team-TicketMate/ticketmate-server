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

    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트처리된 토큰이 요청되었습나다."),

    COOKIES_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠키가 존재하지 않습니다."),

    // OAUTH2

    INVALID_SOCIAL_PLATFORM(HttpStatus.BAD_REQUEST, "잘못된 소셜 플랫폼이 요청되었습니다."),

    // MEMBER

    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다"),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."),

    INVALID_MEMBER_TYPE(HttpStatus.BAD_REQUEST, "잘못된 회원 자격입니다"),

    INVALID_MEMBER_ROLE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 회원 권한 요청입니다."),

    // CONCERT

    CONCERT_NOT_FOUND(HttpStatus.BAD_REQUEST, "콘서트를 찾을 수 없습니다."),

    DUPLICATE_CONCERT_NAME(HttpStatus.BAD_REQUEST, "중복된 공연 제목입니다."),

    INVALID_RANGE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 범위가 입력되었습니다."),

    GENERAL_TICKET_OPEN_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "티켓 일반 예매 오픈일은 필수로 포함되어야 합니다."),

    CONCERT_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 날짜 정보를 찾을 수 없습니다."),

    TICKET_OPEN_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "티켓 오픈일을 찾을 수 없습니다."),

    TICKET_REQUEST_COUNT_EXCEED(HttpStatus.BAD_REQUEST, "티켓 예매 요청 매수가 잘못되었습니다."),

    // CONCERT_HALL

    CONCERT_HALL_NOT_FOUND(HttpStatus.NOT_FOUND, "일치하는 공연장 정보를 찾을 수 없습니다."),

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

    PORTFOLIO_IMG_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "포트폴리오 등록을 위한 이미지는 최대 20장입니다."),

    // REDIS_LOCK
    LOCK_ACQUISITION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "락 획득에 실패했습니다."),

    LOCK_ACQUISITION_INTERRUPT(HttpStatus.INTERNAL_SERVER_ERROR, "락 획득 중 인터럽트가 발생했습니다."),

    // APPLICATION_FORM

    APPLICATION_FORM_NOT_FOUND(HttpStatus.BAD_REQUEST, "대리 티켓팅 신청서를 찾을 수 없습니다."),

    HOPE_AREAS_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "회망구역은 최대 10개까지만 등록 가능합니다."),

    PRIORITY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "요청한 순위가 이미 설정되어있습니다."),

    DUPLICATE_APPLICATION_FROM_REQUEST(HttpStatus.BAD_REQUEST, "중복된 신청서 요청입니다."),

    // NOTIFICATION

    FCM_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "fcm 토큰 획득에 실패했습니다."),

    // EXPRESSIONS

    INVALID_MEMO_REQUEST(HttpStatus.BAD_REQUEST, "기타 거절사유의 메모는 2글자 이상이여야합니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
