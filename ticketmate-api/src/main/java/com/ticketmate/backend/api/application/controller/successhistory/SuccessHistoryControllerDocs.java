package com.ticketmate.backend.api.application.controller.successhistory;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.fulfillmentform.application.dto.successhistory.request.SuccessHistoryFilteredRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.successhistory.response.SuccessHistoryResponse;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;

public interface SuccessHistoryControllerDocs {

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2025-12-30",
      author = "mr6208",
      description = "대리인 성공내역 조회 기능 개발",
      issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/646"
    )
  })
  @Operation(
    summary = "성공내역 조회",
    description = """
       인증된 사용자 한정 자유롭게 성공내역을 조회하는 기능입니다.
                 
       ### 요청 파라미터
       - **agent-id (String)** : 조회할 대리인 ID [필수]
       
       - [SuccessHistoryFilteredRequest]
        - **pageNumber (int)** : 요청할 페이지 번호 [필수X]
        - **pageSize (int)** : 요청할 페이지 사이즈 [필수X]
        - **sortField (String)** : 정렬 필드 [필수X]
        - **sortDirection (String)** : 정렬 방향 [필수X]
        
      ### 반환값 [LIST]
       - **fulfillmentId** : 성공한 성공양식 ID
       - **concertName** : 성공한 콘서트명
       - **concertThumbnailUrl** : 콘서트 썸네일 이미지
       - **concertType** : 공연 타입
       - **createDate** : 생성 시간(성공한 시간이라고 봐도 됩니다)
       - **successHistoryStatus** : 성공내역 상태 [NOT_REVIEWED,REVIEWED] 
       - **reviewId** : 리뷰의 ID
       - **reviewRating** : 리뷰의 평점
       - **clientNickname** : 의뢰인의 닉네임
        
       ### 유의 사항
       - 성공내역 조회는 대리인/의뢰인 모두 사용 가능합니다.   
       - 성공내역은 성공양식 수락시 자동으로 생성됩니다.
       - 성공내역의 상태는 [NOT_REVIEWED, REVIEWED] 두가지 입니다.
       - NOT_REVIEWED 상태일시 리뷰 ID값 및 리뷰 평점은 모두 null값을 반환합니다. 반대로 REVIEWED 상태일시 모두 정상적으로 반환합니다.
       """
  )
  ResponseEntity<Slice<SuccessHistoryResponse>> getSuccessHistoryList
    (CustomOAuth2User customOAuth2User,
      UUID agentId,
      SuccessHistoryFilteredRequest request
    );
}
