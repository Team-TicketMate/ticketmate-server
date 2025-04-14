package com.ticketmate.backend.controller.application.docs;

import com.ticketmate.backend.object.dto.application.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.object.dto.application.request.ApplicationFormRequest;
import com.ticketmate.backend.object.dto.application.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.expressions.request.ApplicationFormRejectRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface ApplicationFormControllerDocs {

    @Operation(
            summary = "대리 티켓팅 신청서 작성",
            description = """

                    이 API는 인증이 필요합니다

                    ### 요청 파라미터
                    - **agentId** (UUID): 대리인 PK [필수]
                    - **concertId** (UUID): 콘서트 PK [필수]
                    - **performanceDate** (LocalDateTime): 공연 일자 [필수]
                    - **requestCount** (Integer): 요청한 티켓 수 [필수]
                    - **hopeAreaList** (List<HopeAreaRequest>): 희망 구역 리스트 [선택]
                    - **requestDetails** (String): 요청 사항 [선택]
                    - **isPreOpen** (Boolean): 선예매 여부 [필수]
    
                    ### 사용 방법
                    `요청 사항`
                    - agentId: 대리인으로 지정된 회원의 ID를 입력합니다.
                    - concertId: 신청할 콘서트의 ID를 입력합니다.
                    - performanceDate: 신청할 공연의 일시를 입력합니다.
                    - requestCount: 요청할 티켓 수를 입력합니다. (최소 1, 최대 공연 티켓 수 제한)
                    - hopeAreaList: 티켓을 원하는 구역을 지정할 수 있습니다.
                    - requestDetails: 추가적인 요청 사항을 입력할 수 있습니다.
                    - isPreOpen: 선예매 여부를 설정합니다. (`true`일 경우 선예매, `false`일 경우 일반 예매)
    
                    ### 유의사항
                    - 대리인(`agentId`)과 의뢰인(`clientId`)은 반드시 올바른 회원 유형이어야 합니다.
                    - 요청된 티켓 수(`requestCount`)는 공연의 티켓 예매 가능 범위 내여야 합니다.
                    - 중복된 신청서는 허용되지 않으며, 이미 대리인에게 신청서를 제출한 경우 오류가 발생합니다.
                    - 희망 구역은 선택 사항으로, 제공된 공연 구역에 맞춰 유효한 구역을 선택해야 합니다.
                    - 선예매와 일반 예매 구분은 `isPreOpen`으로 결정됩니다.
    
                    `TicketOpenDate`
                    - 선예매(`isPreOpen=true`)일 경우, 해당 공연의 선예매 정보가 있어야 합니다.
                    - 일반 예매(`isPreOpen=false`)일 경우, 일반 예매 정보가 있어야 합니다.
    
                    ### 주의사항
                    - 대리인이 아닌 회원이 대리인으로 지정되면 `INVALID_MEMBER_TYPE` 오류가 발생합니다.
                    - 이미 신청서를 제출한 경우, `DUPLICATE_APPLICATION_FROM_REQUEST` 오류가 발생합니다.
                    """
    )
    ResponseEntity<Void> saveApplicationForm(
            CustomOAuth2User customOAuth2User,
            ApplicationFormRequest request);

    @Operation(
            summary = "대리 티켓팅 신청서 필터링 조회",
            description = """

                    이 API는 인증이 필요합니다.

                    ### 요청 파라미터
                    - **clientId** (UUID): 의뢰인 PK [선택]
                    - **agentId** (UUID): 대리인 PK [선택]
                    - **concertId** (UUID): 공연 PK [선택]
                    - **requestCount** (Integer): 매수 [선택]
                    - **applicationStatus** (Enum): 신청서 상태 [선택]
                    - **pageNumber** (Integer): 요청 페이지 번호 [선택]
                    - **pageSize** (Integer): 한 페이지 당 항목 수 [선택]
                    - **sortField** (String): 정렬할 필드 [선택]
                    - **sortDirection** (String): 정렬 방향 [선택]

                    ### 사용 방법
                    `필터링 파라미터`
                    - clientId: 의뢰인이 작성한 신청서를 반환합니다
                    - agentId: 대리인이 받은 신청서를 반환합니다
                    - concertId: 특정 공연에 작성된 신청서를 반환합니다
                    - requestCount: 요청 매수에 따른 신청서를 반환합니다
                    - applicationStatus: 신청서 상태에 따른 신청서를 반환합니다

                    `정렬 조건`
                    - sortField: created_date(기본값), request_count
                    - sortDirection: DESC(기본값), ASC

                    `ApplicationStatus`
                    PENDING("대기")
                
                    APPROVED("승인")
                
                    REJECTED("거절")
                
                    EXPIRED("만료")

                    ### 유의사항
                    - clientId, agentId, concertId, requestCount, applicationStatus 는 요청하지 않을 경우 필터링 조건에 적용되지 않습니다
                    - sortField와 sortDirection은 해당하는 문자열만 입력 가능합니다.
                    """
    )
    ResponseEntity<Page<ApplicationFormFilteredResponse>> filteredApplicationForm(
            CustomOAuth2User customOAuth2User,
            ApplicationFormFilteredRequest request);

    @Operation(
            summary = "대리 티켓팅 신청서 상세 조회",
            description = """

                    이 API는 인증이 필요합니다

                    ### 요청 파라미터
                    - **applicationFormId** (UUID): 조회할 신청서 PK [필수]

                    ### 유의사항
                    """
    )
    ResponseEntity<ApplicationFormFilteredResponse> applicationFormInfo(
            CustomOAuth2User customOAuth2User,
            UUID applicationFormId);

    @Operation(
            summary = "대리 티켓팅 신청서 거절",
            description = """

                    이 API는 인증이 필요합니다

                    ### 요청 파라미터
                    - **applicationFormId** (UUID): 조회할 신청서 PK [필수]
                    - **applicationFormRejectedType** (String): 거절사유 [필수]
                    - **otherMemo** (String): 거절사유 '기타'일 시 작성할 메모                  
                    
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
}
