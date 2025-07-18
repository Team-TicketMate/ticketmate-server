package com.ticketmate.backend.global.exception;

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

  INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),

  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),

  EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

  REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다."),

  COOKIES_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠키가 존재하지 않습니다."),

  VERIFY_CODE_EXPIRED_OR_NOT_FOUND(HttpStatus.NOT_FOUND, "인증코드가 만료되었거나, 존재하지 않습니다."),

  VERIFY_CODE_NOT_SAME(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),

  // OAUTH2

  INVALID_SOCIAL_PLATFORM(HttpStatus.BAD_REQUEST, "잘못된 소셜 플랫폼이 요청되었습니다."),

  INVALID_REDIRECT_URI(HttpStatus.BAD_REQUEST, "유효하지 않은 리다이렉트 URI가 요청되었습니다."),

  // MEMBER

  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 가입된 이메일입니다"),

  MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."),

  INVALID_MEMBER_TYPE(HttpStatus.BAD_REQUEST, "잘못된 회원 자격입니다"),

  INVALID_MEMBER_ROLE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 회원 권한 요청입니다."),

  // MEMBER FOLLOW

  SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신은 팔로우 할 수 없습니다."),

  CLIENT_FOLLOW_AGENT_ONLY(HttpStatus.BAD_REQUEST, "의뢰인만 대리인을 팔로우 할 수 있습니다."),

  DUPLICATE_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "이미 팔로우한 회원입니다."),

  UNFOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "팔로우하지 않은 회원에 대해 언팔로우할 수 없습니다."),

  // CONCERT

  CONCERT_NOT_FOUND(HttpStatus.BAD_REQUEST, "콘서트를 찾을 수 없습니다."),

  DUPLICATE_CONCERT_NAME(HttpStatus.CONFLICT, "중복된 공연 제목입니다."),

  INVALID_RANGE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 범위가 입력되었습니다."),

  TICKET_OPEN_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "티켓 선예매/일반예매 오픈일은 필수로 포함되어야 합니다."),

  INVALID_TICKET_REQUEST_MAX_COUNT(HttpStatus.BAD_REQUEST, "티켓 최대 요청 개수 입력이 잘못되었습니다."),

  PRE_OPEN_COUNT_EXCEED(HttpStatus.BAD_REQUEST, "선예매 오픈일 데이터는 최대 한개까지만 등록 가능합니다."),

  GENERAL_OPEN_COUNT_EXCEED(HttpStatus.BAD_REQUEST, "일반예매 오픈일 데이터는 최대 한개까지만 등록 가능합니다."),

  CONCERT_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "공연일 데이터는 필수로 포함되어야 합니다."),

  CONCERT_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 날짜 정보를 찾을 수 없습니다."),

  INVALID_CONCERT_DATE(HttpStatus.BAD_REQUEST, "잘못된 공연일자입니다."),

  DUPLICATE_CONCERT_DATE(HttpStatus.CONFLICT, "중복된 공연일자입니다."),

  TICKET_OPEN_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "티켓 오픈일을 찾을 수 없습니다."),

  INVALID_TICKET_OPEN_DATE(HttpStatus.BAD_REQUEST, "잘못된 티켓오픈일입니다."),

  TICKET_OPEN_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "티켓 오픈 타입을 찾을 수 없습니다."),

  TICKET_REQUEST_COUNT_EXCEED(HttpStatus.BAD_REQUEST, "티켓 예매 요청 매수가 초과되었습니다."),

  // CONCERT_HALL

  CONCERT_HALL_NOT_FOUND(HttpStatus.NOT_FOUND, "일치하는 공연장 정보를 찾을 수 없습니다."),

  DUPLICATE_CONCERT_HALL_NAME(HttpStatus.CONFLICT, "중복된 공연장 이름입니다."),

  CONCERT_HALL_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "공연장 저장 중 오류가 발생했습니다."),

  CITY_NOT_FOUND(HttpStatus.BAD_REQUEST, "주소에 일치하는 city값이 없습니다."),

  // MOCK DATA TODO: 출시 후 삭제

  GENERATE_MOCK_DATA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Mock 데이터 생성 중 오류 발생"),

  SAVE_MOCK_DATA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Mock 데이터 저장 중 오류 발생"),

  // S3

  INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 확장자입니다."),

  FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),

  S3_UPLOAD_AMAZON_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 서비스 에러로 인해 파일 업로드에 실패했습니다."),

  S3_UPLOAD_AMAZON_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 클라이언트 에러로 인해 파일 업로드에 실패했습니다."),

  S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드 중 오류 발생"),

  S3_DELETE_AMAZON_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 서비스 에러로 인해 파일 삭제에 실패했습니다."),

  S3_DELETE_AMAZON_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 클라이언트 에러로 인해 파일 삭제에 실패했습니다."),

  S3_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제 중 오류 발생"),

  INVALID_FILE_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 요청입니다."),

  INVALID_FILE_PATH(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 URL 요청입니다."),

  // PORTFOLIO

  PORTFOLIO_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "포트폴리오 업로드에 실패했습니다"),

  PORTFOLIO_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 포트폴리오를 찾을 수 없습니다."),

  INVALID_PORTFOLIO_IMG_COUNT(HttpStatus.BAD_REQUEST, "포트폴리오 이미지 첨부파일은 최소 1개, 최대 20개까지 등록 가능합니다"),

  INVALID_PORTFOLIO_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 포트폴리오 타입 입니다."),

  // REDIS_LOCK

  LOCK_ACQUISITION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "락 획득에 실패했습니다."),

  LOCK_ACQUISITION_INTERRUPT(HttpStatus.INTERNAL_SERVER_ERROR, "락 획득 중 인터럽트가 발생했습니다."),

  // APPLICATION_FORM

  APPLICATION_FORM_NOT_FOUND(HttpStatus.BAD_REQUEST, "대리 티켓팅 신청서를 찾을 수 없습니다."),

  HOPE_AREAS_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "회망구역은 최대 10개까지만 등록 가능합니다."),

  PRIORITY_ALREADY_EXISTS(HttpStatus.CONFLICT, "요청한 순위가 이미 설정되어있습니다."),

  DUPLICATE_APPLICATION_FROM_REQUEST(HttpStatus.CONFLICT, "중복된 신청서 요청입니다."),

  ALREADY_ACCEPTED_APPLICATION_FROM(HttpStatus.CONFLICT, "이미 수락된 신청서입니다."),

  INVALID_APPLICATION_FORM_STATUS(HttpStatus.BAD_REQUEST, "잘못된 신청서 상태입니다."),

  APPLICATION_FORM_DETAIL_REQUIRED(HttpStatus.BAD_REQUEST, "신청서에는 최소 1개 이상의 공연일자가 포함되어야합니다."),

  APPLICATION_FORM_DETAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "신청서 세부사항 값을 찾을 수 없습니다."),

  DUPLICATE_APPLICATION_FORM_DETAIL(HttpStatus.CONFLICT, "중복된 신청서 세부사항입니다."),

  // NOTIFICATION

  FCM_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "fcm 토큰 획득에 실패했습니다."),

  // REJECTION_REASON

  INVALID_MEMO_REQUEST(HttpStatus.BAD_REQUEST, "기타 거절사유의 메모는 2글자 이상이여야합니다."),

  REJECTION_REASON_NOT_FOUND(HttpStatus.NOT_FOUND, "신청서의 거절 사유를 찾을 수 없습니다."),

  // CHAT

  CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾지 못했습니다."),

  ALREADY_EXIST_CHAT_ROOM(HttpStatus.CONFLICT, "이미 해당 콘서트, 대리인, 의뢰인 및 선예매/일반예매에 관한 채팅방이 존재합니다."),

  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾지 못했습니다."),

  NO_AUTH_TO_ROOM(HttpStatus.FORBIDDEN, "해당 사용자는 이 채팅방에 들어갈 수 없습니다."),

  // EMBEDDING

  EMBEDDING_API_ERROR(HttpStatus.BAD_REQUEST, "Vertex AI API 호출에 실패했습니다."),

  EMBEDDING_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "반환된 임베딩 데이터가 존재하지 않습니다."),

  // PAGEABLE

  INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "필터링 조회 시 정렬 필드 요청이 잘못되었습니다."),

  // SMS

  MESSAGE_NOT_RECEIVED(HttpStatus.BAD_REQUEST, "SMS 발송 - 메시지 접수 실패"),

  SMS_EMPTY_RESPONSE(HttpStatus.BAD_REQUEST, "SMS 발송 - SMS 서버로 부터 아무런 응답을 받지 못했습니다"),

  SMS_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송 - 알 수 없는 오류 발생"),

  SMS_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 전송 예외 발생"),

  ;

  private final HttpStatus status;
  private final String message;
}
