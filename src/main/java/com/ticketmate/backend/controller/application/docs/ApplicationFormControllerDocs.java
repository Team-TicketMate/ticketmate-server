package com.ticketmate.backend.controller.application.docs;

import com.ticketmate.backend.object.dto.application.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.object.dto.application.request.ApplicationFormRequest;
import com.ticketmate.backend.object.dto.application.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
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
                    - **requestCount** (Integer): 티켓 요청 매수 [필수] (default: 1)
                    - **hopeAreas** (Map<String, String>): 희망구역
                    - **requestDetails** (String): 요청사항

                    ### 유의사항
                    - 티켓 요청 매수는 1 이상의 정수를 입력해주세요
                    - 희망구역은 최대 10개까지 등록 가능합니다
                    """
    )
    ResponseEntity<Void> saveApplicationForm(
            CustomOAuth2User customOAuth2User,
            ApplicationFormRequest request);

    @Operation(
            summary = "대리 티켓팅 신청서 필터링 조회",
            description = """

                    이 API는 인증이 필요합니다

                    ### 요청 파라미터
                    - **clientId** (UUID): 의뢰인 PK [선택]
                    - **agentId** (UUID): 대리인 PK [선택]
                    - **concertId** (UUID): 콘서트 PK [선택]
                    - **requestCount** (Integer): 매수 [선택]
                    - **applicationStatus** (enum): 신청서 상태 [선택]
                    - **pageNumber** (Integer): 요청 페이지 번호 [선택]
                    - **pageSize** (Integer): 한 페이지 당 항목 수 [선택]
                    - **sortField** (String): 정렬할 필드 [선택]
                    - **sortDirection** (String): 정렬 방향 [선택]
                    
                    ### 사용 방법
                    `필터링 파라미터`
                    - clientId: 해당 의뢰인이 작성한 신청서를 반환합니다
                    - agentId: 해당 대리인에게 요청된 신청서를 반환합니다
                    - concertId: 해당 콘서트에 작성된 신청서를 반환합니다
                    - requestCount: 특정 매수를 요청한 신청서를 반환합니다 (ex. 티켓 3장을 요청한 신청서만 반환)
                    - applicationStatus: 특정 신청 상태의 신청서를 반환합니다
                    
                    `정렬 조건`
                    - sortField: created_date(기본값), request_count
                    - sortDirection: ASC, DESC(기본값)

                    `ApplicationStatus`
                    
                    PENDING("대기")
                    APPROVED("승인")
                    REJECTED("거절")
                    EXPIRED("만료")

                    ### 유의사항
                    - clientId, agentId, concertId, requestCount, applicationStatus 는 요청하지 않을 경우 필터링 조건에 적용되지 않습니다
                    - sortField, sortType은 해당하는 문자열만 입력 가능합니다.
                    """
    )
    ResponseEntity<Page<ApplicationFormInfoResponse>> filteredApplicationForm(
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
    ResponseEntity<ApplicationFormInfoResponse> applicationFormInfo(
            CustomOAuth2User customOAuth2User,
            UUID applicationFormId);
}
