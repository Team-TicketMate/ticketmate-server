package com.ticketmate.backend.api.application.controller.applicationform;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormEditRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.applicationform.application.dto.response.RejectionReasonResponse;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ApplicationFormControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-28",
          author = "Chuseok22",
          description = "희망구역 최대 개수 5개 제한",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/579"
      ),
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
            - **hopeAreaList** (배열): 희망 구역 목록 [선택, 최대 5개]
              - **priority** (Integer): 희망 구역 우선순위 [필수, 1~5 사이 값]
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

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-07",
          author = "Chuseok22",
          description = "SortField 정렬 필드 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/393"
      ),
      @ApiChangeLog(
          date = "2025-07-04",
          author = "Chuseok22",
          description = "신청서 필터링 조회 반환값 변경",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/388"
      )
  })
  @Operation(
      summary = "신청서 필터링 조회",
      description = """
          다양한 필터링 옵션을 제공하여 신청서 목록을 페이징 조회합니다.
          
          ### 요청 파라미터
          - `clientId` (UUID, optional): 의뢰인 PK
          - `agentId` (UUID, optional): 대리인 PK
          - `concertId` (UUID, optional): 공연 PK
          - `applicationFormStatusSet` (Set<ApplicationFormStatus>, optional): 조회할 신청서 상태 목록
          - `pageNumber` (Integer, optional, default = 1): 조회할 페이지 번호 (1부터 시작)
          - `pageSize` (Integer, optional, default = 10): 한 페이지당 데이터 개수
          - `sortField` (String, optional, default = "CREATED_DATE"): 정렬 필드 (`CREATED_DATE`, `REQUEST_COUNT`)
          - `sortDirection` (String, optional, default = "DESC"): 정렬 방향 (`ASC`, `DESC`)
          
          ### 응답 데이터
          - HTTP 200
          - `Page<ApplicationFormFilteredResponse>` 형태의 페이징 결과
            - `content`: 신청서 객체 리스트
            - `pageable`: 요청된 페이지 정보
            - `totalElements`: 전체 조회 건수
          
          #### ApplicationFormFilteredResponse 필드
          - `applicationFormId` (UUID): 신청서 PK
          - `concertName` (String): 공연 제목
          - `concertThumbnailUrl` (String): 공연 썸네일 URL
          - `agentNickname` (String): 대리인 닉네임
          - `clientNickname` (String): 의뢰인 닉네임
          - `submittedDate` (String, yyyy-MM-dd'T'HH:mm:ss): 신청 일시 (Asia/Seoul)
          - `applicationFormStatus` (ApplicationFormStatus): 신청서 상태
          - `ticketOpenType` (TicketOpenType): 선·일반 예매 타입
          
          ### 사용 방법 & 유의사항
          1. 모든 필터링 파라미터는 선택 사항이며, 미입력 시 해당 조건은 전체 조회됩니다.
          2. `pageNumber`와 `pageSize`는 1 이상의 정수만 허용됩니다.
          3. `sortField`에는 `"CREATED_DATE"` 또는 `"REQUEST_COUNT"`만 입력 가능합니다. (UPPER_CASE)
          4. `sortDirection`에는 `"ASC"` 또는 `"DESC"`만 입력 가능합니다.
          5. 잘못된 파라미터 값(형식, 범위 위반 등)을 보내면 400 Bad Request가 반환됩니다.
          """
  )
  ResponseEntity<Page<ApplicationFormFilteredResponse>> filteredApplicationForm(
      CustomOAuth2User customOAuth2User,
      ApplicationFormFilteredRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-17",
          author = "Chuseok22",
          description = "신청서 상세 조회 반환값에 '신청서 상태', '선예매/일반예매' 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/550"
      ),
      @ApiChangeLog(
          date = "2025-07-08",
          author = "Chuseok22",
          description = "신청서 상세 조회 반환값 수정",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/402"
      )
  })
  @Operation(
      summary = "대리 티켓팅 신청서 상세 조회",
      description = """
          특정 신청서의 상세 정보를 조회합니다.
          
          ### 인증 필요
          
          ### 경로 변수
          - **applicationFormId** (UUID): 조회할 신청서 PK [필수]
          
          ### 반환 데이터 (ApplicationFormInfoResponse)
          - **concertInfoResponse** (ConcertInfoResponse)
            - concertName: 공연명
            - concertHallName: 공연장 이름
            - concertThumbnailUrl: 공연 썸네일 URL
            - seatingChartUrl: 좌석 배치도 URL
            - concertType: 공연 카테고리
            - concertDateInfoResponseList: List<ConcertDateInfoResponse>
              - performanceDate: 공연 일시
              - session: 회차
            - ticketOpenDateInfoResponseList: List<TicketOpenDateInfoResponse>
              - openDate: 티켓 오픈일
              - requestMaxCount: 최대 예매 매수
              - isBankTransfer: 무통장 입금 여부
              - ticketOpenType: 선예매/일반예매 구분
            - ticketReservationSite: 예매처
          
          - **applicationFormDetailResponseList**: List<ApplicationFormDetailResponse>
            - performanceDate: 공연 일시
            - session: 회차
            - requestCount: 요청 매수
            - hopeAreaResponseList: List<HopeAreaResponse>
              - priority: 우선순위
              - location: 위치
              - price: 가격
            - requirement: 요청 사항
          
          - applicationFormStatus: 신청서 상태
          - ticketOpenType: 선예매/일반예매
          
          ### 주요 오류 코드
          - APPLICATION_FORM_NOT_FOUND: 신청서를 찾을 수 없음
          """
  )
  ResponseEntity<ApplicationFormInfoResponse> getApplicationFormInfo(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-28",
          author = "Chuseok22",
          description = "희망구역 최대 개수 5개 제한",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/579"
      ),
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
          ### 인증 필요
          - '의뢰인'만 가능합니다
          
          로그인된 의뢰인이 본인이 작성한 신청서의 세부 정보를 수정합니다.
          - **대리인**, **공연**, **선예매/일반예매** 구분은 변경 불가
          - **신청서에 기존에 작성되었던 신청서 세부사항 정보를 모두 삭제 후 다시 저장합니다.**
          - **기존 데이터가 모두 삭제되므로, 일부만 수정시에도 모든 데이터를 같이 요청해야합니다.**
          - 수정 가능한 신청서 상태: CANCELED, REJECTED, CANCELED_IN_PROCESS
          
          **요청 파라미터 (Path Variable)**
          - `application-form-id` (UUID): 수정할 신청서 PK
          
          **요청 본문 (Request Body)**
          ```json
          {
            "applicationFormDetailRequestList": [
              {
                "performanceDate": "2025-06-30T19:00:00",
                "requestCount": 2,
                "hopeAreaList": [
                  {
                    "priority": 1,
                    "location": "A구역",
                    "price": 150000
                  },
                  {
                    "priority": 2,
                    "location": "B구역",
                    "price": 100000
                  }
                ],
                "requestDetails": "가까운 좌석으로 부탁드립니다."
              },
              {
                "performanceDate": "2025-07-01T20:00:00",
                "requestCount": 1,
                "hopeAreaList": [
                  {
                    "priority": 1,
                    "location": "C구역",
                    "price": 120000
                  }
                ],
                "requestDetails": "무대 오른쪽 앞줄이면 좋겠습니다."
              }
            ]
          }
          ```
          - `ApplicationFormEditRequest applicationFormEditRequest`를 json 형태로 작성
          - `performanceDate` (LocalDateTime, 필수): 공연 일자 (중복 불가)
          - `requestCount` (Integer, 필수): 요청 매수 (최소 1장, 최대 공연별 최대 매수)
          - `hopeAreaList` (List<HopeAreaRequest>, 선택): 희망 구역 리스트 (최대 5개)
          - `requestDetails` (String, 선택): 요청사항
          
          **응답**
          - HTTP 200 OK: 수정 성공 (응답 본문 없음)
          """
  )
  ResponseEntity<Void> editApplicationForm(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId,
      ApplicationFormEditRequest applicationFormEditRequest);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-03",
          author = "Chuseok22",
          description = "신청서 취소 API 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/287"
      ),
      @ApiChangeLog(
          date = "2025-07-02",
          author = "Chuseok22",
          description = "신청서 취소 API 작성",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/287"
      )
  })
  @Operation(
      summary = "신청서 취소",
      description = """
          로그인된 의뢰인이 본인이 작성한 신청서를 취소합니다.
          - 취소 가능한 신청서 상태: PENDING
          
          **요청 파라미터 (Path Variable)**
          - `application-form-id` (UUID): 취소할 신청서 PK
          
          **요청 본문 (Request Body)**
          `없음`
          
          **응답**
          - HTTP 200 OK: 취소 성공 (응답 본문 없음)
          """
  )
  ResponseEntity<Void> cancelApplicationForm(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-03",
          author = "Chuseok22",
          description = "신청서 거절 API 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/287"
      )
  })
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
          - ACCESS_DENIED: 접근이 거부되었습니다. (해당 대리인에게 작성된 신청서가 아닌경우)
          - INVALID_APPLICATION_FORM_STATUS: 잘못된 신청서 상태입니다.
          
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
  ResponseEntity<Void> rejectApplicationForm(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId, ApplicationFormRejectRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-08-01",
          author = "Chuseok22",
          description = "신청서 수락 시 채팅방 자동 생성 제거",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/444"
      ),
      @ApiChangeLog(
          date = "2025-07-03",
          author = "Chuseok22",
          description = "신청서 수락 API 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/287"
      )
  })
  @Operation(
      summary = "대리 티켓팅 신청서 수락",
      description = """
          대리인이 의뢰인의 티켓팅 신청서를 승인합니다.
          
          ### 인증 필요
          - 대리인(AGENT) 타입의 사용자만 요청 가능합니다.
          
          ### 경로 변수
          - **application-form-id** (UUID): 승인할 신청서 PK [필수]
          
          ### 반환 데이터
          - `없음`
          
          ### 유의사항
          - 해당 API는 대리인만 사용 가능합니다.
          - 해당 API 호출 시 의뢰인에게 수락됐다는 알림이 발송됩니다.
          - 콘서트에 대한 신청폼이 이미 수락상태인경우 대리자는 수락이 불가합니다.
          - 이미 채팅방이 존재한다면 채팅방의 신청폼에 대한 정보만 수정합니다.
          """
  )
  ResponseEntity<Void> approve(
      CustomOAuth2User customOAuth2User,
      UUID applicationFormId);

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
          - '대기', '승인' 된 신청서에 대해서 중복 조회를 진행합니다
          """
  )
  ResponseEntity<Boolean> isDuplicateApplicationForm(
      CustomOAuth2User customOAuth2User,
      ApplicationFormDuplicateRequest request
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-03",
          author = "Chuseok22",
          description = "신청서 거절 사유 조회 API 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/378"
      )
  })
  @Operation(
      summary = "신청서 거절 사유 조회 API",
      description = """
          클라이언트에서 전달한 신청서 ID(applicationFormId)를 기반으로 해당 신청서에 대한 거절 사유 정보를 조회합니다.
          
          ### 인증 필요
          
          ### 경로 변수
          - **application-form-id** (UUID): 신청서 PK [필수]
          
          ### 반환 데이터
          - **applicationFormRejectedType**: 거절 사유 타입
          - **otherMemo**: 기타 메모 (applicationFormRejectedType이 "OTHER" 인 경우에만 작성됩니다)
          """
  )
  ResponseEntity<RejectionReasonResponse> getRejectionReason(
      CustomOAuth2User customOAuth2User, UUID applicationFormId);
}
