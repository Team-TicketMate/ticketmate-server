package com.ticketmate.backend.domain.applicationform.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDetailRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ApplicationFormControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-24",
          author = "Chuseok22",
          description = "신청서 작성 API 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/332"
      )
  })
  @Operation(
      summary = "신청서 작성",
      description = """
          의뢰인이 대리인에게 티켓팅을 요청하기 위한 신청서를 작성합니다.
          
          ### 인증 필요
          - 의뢰인(CLIENT) 타입의 사용자만 요청 가능합니다.
          
          ### 요청 본문
          - **agentId** (UUID): 대리인 PK [필수]
          - **concertId** (UUID): 공연 PK [필수]
          - **applicationFormDetailRequestList** (배열): 신청 공연 회차 목록 [최소 1개 이상 필수]
            - **performanceDate** (String): 공연 일시 (yyyy-MM-dd'T'HH:mm:ss 형식) [필수]
            - **requestCount** (Integer): 요청 매수 [필수, 최소 1장]
            - **hopeAreaList** (배열): 희망 구역 목록
              - **priority** (Integer): 희망 구역 우선순위 [필수, 1~10 사이 값]
              - **location** (String): 희망 구역 위치명 [필수, 예: "A구역"]
              - **price** (Long): 희망 구역 가격 [필수, 0원 이상]
            - **requestDetails** (String): 요청 사항
          - **ticketOpenType** (Enum): 선예매/일반예매 구분 [필수]
            - GENERAL_OPEN: 일반예매
            - PRE_OPEN: 선예매
          
          ### 주요 오류 코드
          - MEMBER_NOT_FOUND: 존재하지 않는 회원
          - INVALID_MEMBER_TYPE: 부적절한 회원 타입 (대리인이 아님)
          - CONCERT_NOT_FOUND: 존재하지 않는 공연
          - DUPLICATE_APPLICATION_FROM_REQUEST: 이미 해당 공연에 대한 신청서 존재
          - APPLICATION_FORM_DETAIL_REQUIRED: 공연 회차 정보 누락
          - TICKET_OPEN_DATE_NOT_FOUND: 티켓 오픈일 정보 없음
          - TICKET_REQUEST_COUNT_EXCEED: 요청 매수 초과
          """
  )
  ResponseEntity<Void> saveApplicationForm(
      CustomOAuth2User customOAuth2User,
      ApplicationFormRequest request);

  @Operation(
      summary = "신청서 필터링 조회",
      description = """
          신청서 목록을 필터링하여 조회합니다.
          
          ### 인증 필요
          
          ### 요청 파라미터
          - **clientId** (UUID): 의뢰인 PK [선택, 생략 시 모든 의뢰인]
          - **agentId** (UUID): 대리인 PK [선택, 생략 시 모든 대리인]
          - **concertId** (UUID): 공연 PK [선택, 생략 시 모든 공연]
          - **applicationFormStatus** (Enum): 신청서 상태 [선택]
            - PENDING: 대기
            - ACCEPTED: 승인
            - REJECTED: 거절
            - CANCELED: 취소
            - CANCELED_IN_PROCESS: 진행 취소
          - **pageNumber** (Integer): 페이지 번호 [선택, 기본값 1]
          - **pageSize** (Integer): 페이지 크기 [선택, 기본값 10]
          - **sortField** (String): 정렬 기준 [선택, 기본값 created_date]
            - created_date: 생성일 기준
            - total_request_count: 요청 매수 기준
          - **sortDirection** (String): 정렬 방향 [선택, 기본값 DESC]
            - ASC: 오름차순
            - DESC: 내림차순
          
          ### 반환 데이터
          - Page<ApplicationFormFilteredResponse>: 페이지네이션된 신청서 목록
            - content: 신청서 정보 배열
            - totalElements: 전체 항목 수
            - totalPages: 전체 페이지 수
            - number: 현재 페이지 번호
            - size: 페이지 크기
          
          ### 주요 오류 코드
          - INVALID_MEMBER_TYPE: 부적절한 회원 타입
          - MEMBER_NOT_FOUND: 존재하지 않는 회원
          - CONCERT_NOT_FOUND: 존재하지 않는 공연
          """
  )
  ResponseEntity<Page<ApplicationFormFilteredResponse>> filteredApplicationForm(
      CustomOAuth2User customOAuth2User,
      ApplicationFormFilteredRequest request);

  @Operation(
      summary = "대리 티켓팅 신청서 상세 조회",
      description = """
          특정 신청서의 상세 정보를 조회합니다.
          
          ### 인증 필요
          
          ### 경로 변수
          - **applicationFormId** (UUID): 조회할 신청서 PK [필수]
          
          ### 반환 데이터
          - ApplicationFormFilteredResponse: 신청서 상세 정보
            - applicationFormId: 신청서 ID
            - clientId: 의뢰인 ID
            - agentId: 대리인 ID
            - concertId: 공연 ID
            - openDate: 티켓 오픈일
            - applicationFormDetailResponseList: 신청 회차 정보 목록
              - performanceDate: 공연 일시
              - session: 회차
              - requestCount: 요청 매수
              - hopeAreaResponseList: 희망 구역 목록
                - priority: 우선순위
                - location: 위치
                - price: 가격
              - requirement: 요청 사항
            - totalRequestCount: 전체 요청 매수
            - applicationFormStatus: 신청서 상태
            - ticketOpenType: 선예매/일반예매 구분
          
          ### 주요 오류 코드
          - APPLICATION_FORM_NOT_FOUND: 신청서를 찾을 수 없음
          """
  )
  ResponseEntity<ApplicationFormFilteredResponse> applicationFormInfo(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-24",
          author = "Chuseok22",
          description = "신청서 수정 API 작성",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/332"
      )
  })
  @Operation(
      summary = "신청서 수정",
      description = """
          로그인된 의뢰인이 본인이 작성한 신청서의 세부 정보를 수정합니다.
          - **대리인**, **공연**, **선예매/일반예매** 구분은 변경 불가
          - 수정 가능한 신청서 상태: CANCELED, REJECTED, CANCELED_IN_PROCESS
          
          **요청 파라미터 (Path Variable)**
          - `application-form-id` (UUID): 수정할 신청서 PK
          
          **요청 본문 (Request Body)**
          ```json
          [
            {
              "performanceDate": "2025-02-11T20:00:00",
              "requestCount": 2,
              "hopeAreaList": [
                { "priority": 1, "location": "A구역", "price": 120000 }
              ],
              "requestDetails": "빠른 답변 부탁드립니다."
            },
            ...
          ]
          ```
          - `List<ApplicationFormDetailRequest> applicationFormDetailRequestList`를 json 형태로 작성
          - `performanceDate` (LocalDateTime, 필수): 공연 일자 (중복 불가)
          - `requestCount` (Integer, 필수): 요청 매수 (최소 1장, 최대 공연별 최대 매수)
          - `hopeAreaList` (List<HopeAreaRequest>, 선택): 희망 구역 리스트 (최대 10개)
          - `requestDetails` (String, 선택): 요청사항
          
          **응답**
          - HTTP 200 OK: 수정 성공 (응답 본문 없음)
          
          **예외 처리**
          | 에러 코드                             | HTTP 상태 | 메시지                                      | 상황 설명                             |
          |---------------------------------------|----------|---------------------------------------------|--------------------------------------|
          | `APPLICATION_FORM_NOT_FOUND`          | 404      | "신청서를 찾을 수 없습니다."                | 해당 ID의 신청서가 없을 때           |
          | `ACCESS_DENIED`                       | 403      | "접근이 거부되었습니다."                    | 본인 소유 신청서가 아닐 때           |
          | `INVALID_APPLICATION_FORM_STATUS`     | 400      | "수정 불가능한 신청서 상태입니다."          | 상태가 PENDING/REJECTED 이외일 때    |
          | `APPLICATION_FORM_DETAIL_REQUIRED`    | 400      | "신청서 상세정보가 필요합니다."             | 최소 1개 이상 세부정보 없을 때       |
          | `INVALID_CONCERT_DATE`                | 400      | "유효하지 않은 공연일자입니다."             | 공연일자가 null 일 때                |
          | `DUPLICATE_CONCERT_DATE`              | 400      | "중복된 공연일자가 존재합니다."             | 동일 공연일자가 2개 이상 전달될 때   |
          | `TICKET_REQUEST_COUNT_EXCEED`         | 400      | "요청 매수가 허용 범위를 벗어났습니다."     | 매수 <1 또는 >maxCount 일 때         |
          """
  )
  ResponseEntity<Void> editApplicationForm(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId,
      List<ApplicationFormDetailRequest> applicationFormDetailRequestList);

  @Operation(
      summary = "대리 티켓팅 신청서 거절",
      description = """
          대리인이 의뢰인의 티켓팅 신청서를 거절합니다.
          
          ### 인증 필요
          - 대리인(AGENT) 타입의 사용자만 요청 가능합니다.
          
          ### 경로 변수
          - **application-form-id** (UUID): 거절할 신청서 PK [필수]
          
          ### 요청 본문
          - **applicationFormRejectedType** (Enum): 거절 사유 [필수]
          - **otherMemo** (String): 기타 사유인 경우 상세 메모 [기타 사유인 경우 필수, 2자 이상]
          
          ### 주요 오류 코드
          - INVALID_MEMBER_TYPE: 부적절한 회원 타입 (대리인이 아님)
          - APPLICATION_FORM_NOT_FOUND: 신청서를 찾을 수 없음
          - INVALID_MEMO_REQUEST: 유효하지 않은 메모 (기타 사유인데 메모가 2자 미만)
          
          ### ApplicationRejectedType
          
          FEE_NOT_MATCHING_MARKET_PRICE("수고비가 시세에 맞지 않음")
          
          RESERVATION_CLOSED("예약 마감")
          
          SCHEDULE_UNAVAILABLE("티켓팅 일정이 안됨")
          
          OTHER("기타")
          
          ### 유의사항
          - 해당 API는 대리인만 사용 가능합니다.
          - 거절사유가 OTHER('기타') 일 시 otherMemo는 2자 이상이여야 합니다.
          - 거절 사유가 OTHER이 아닐 시에는 otherMemo는 공백처리해서 주시면 됩니다.
          """
  )
  void reject(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId, ApplicationFormRejectRequest request);

  @Operation(
      summary = "대리 티켓팅 신청서 수락",
      description = """
          대리인이 의뢰인의 티켓팅 신청서를 승인합니다.
          승인 시 의뢰인과 대리인 간의 채팅방이 자동으로 생성됩니다.
          
          ### 인증 필요
          - 대리인(AGENT) 타입의 사용자만 요청 가능합니다.
          
          ### 경로 변수
          - **application-form-id** (UUID): 승인할 신청서 PK [필수]
          
          ### 반환 데이터
          - String: 생성된 채팅방 ID
          
          ### 주요 오류 코드
          - INVALID_MEMBER_TYPE: 부적절한 회원 타입 (대리인이 아님)
          - APPLICATION_FORM_NOT_FOUND: 신청서를 찾을 수 없음
          - INVALID_APPLICATION_FORM_STATUS: 유효하지 않은 신청서 상태 (대기 상태가 아님)
          - ALREADY_APPROVED_APPLICATION_FROM: 이미 다른 신청서가 승인된 상태
          
          ### 유의사항
          - 해당 API는 대리인만 사용 가능합니다.
          - 해당 API 호출 시 의뢰인에게 수락됐다는 알림이 발송됩니다.
          - 해당 API 호출 시 의뢰인과 대리자에 관한 1:1 채팅방이 생성됩니다.
          - 콘서트에 대한 신청폼이 이미 수락상태인경우 대리자는 수락이 불가합니다.
          - 이미 채팅방이 존재한다면 채팅방의 신청폼에 대한 정보만 수정합니다.
          """
  )
  ResponseEntity<String> approve(
      CustomOAuth2User customOAuth2User, UUID applicationFormId);

  @Operation(
      summary = "신청서 중복 확인",
      description = """
          이미 의뢰인이 대리인에게 해당 공연(선예매/일반예매 구분)에 대한 신청서를 작성했는지 확인합니다.
          
          ### 인증 필요
          
          ### 요청 파라미터
          - **agentId** (UUID): 대리인 PK [필수]
          - **concertId** (UUID): 공연 PK [필수]
          - **ticketOpenType** (Enum): 선예매/일반예매 타입 [필수]
            - GENERAL_OPEN: 일반예매
            - PRE_OPEN: 선예매
          
          ### 반환 데이터
          - Boolean: 중복 여부 (true: 중복, false: 중복 아님)
          
          ### 활용 방법
          - 의뢰인이 신청서 작성 전 중복 확인용으로 사용
          - 중복(true)인 경우 이미 신청한 신청서가 있으므로 추가 신청 불필요
          """
  )
  ResponseEntity<Boolean> isDuplicateApplicationForm(
      CustomOAuth2User customOAuth2User,
      ApplicationFormDuplicateRequest request
  );
}
