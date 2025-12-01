package com.ticketmate.backend.api.application.controller.review;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.review.application.dto.request.AgentCommentRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewEditRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewFilteredRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewRequest;
import com.ticketmate.backend.review.application.dto.response.ReviewFilteredResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ReviewControllerDocs {
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-05",
          author = "Yooonjeong",
          description = "리뷰 CRUD 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/533"
      )
  })
  @Operation(
      summary = "리뷰 단건 조회",
      description = """
        ### 요청 파라미터
        - `review-id` (UUID, path, required): 조회할 리뷰 ID

        ### 응답 데이터 (`ReviewInfoResponse`)
        - `reviewId` (UUID): 리뷰 ID
        - `concertTitle` (String): 공연 제목
        - `rating` (float): 별점
        - `comment` (String): 리뷰 내용
        - `reviewImgList` (List<ReviewImgResponse>): 리뷰 이미지 목록
            - `reviewImgId` (UUID): 리뷰 이미지 ID
            - `reviewImgUrl` (String): 리뷰 이미지 접근 URL
        - `createdDate` (LocalDateTime): 리뷰 생성 일시
        - `agentComment` (AgentCommentResponse): 대리인 댓글 정보
            - `agentNickname` (String): 대리인 닉네임
            - `agentProfileUrl` (String): 대리인 프로필 이미지 URL
            - `comment` (String): 대리인 댓글 내용
            - `commentedDate` (LocalDateTime): 댓글 작성 일시

        ### 사용 방법
        - 경로 변수 `review-id`를 전달하여 단건 조회합니다.

        ### 유의 사항
        - 존재하지 않는 리뷰 ID로 요청 시 예외가 발생합니다.
        - 댓글이 없는 경우 `agentComment`는 null로 반환됩니다.

        ### 예외 처리
        - `REVIEW_NOT_FOUND` (404): "해당 리뷰를 찾을 수 없습니다."
        """
  )
  ResponseEntity<ReviewInfoResponse> getReviewInfo(UUID reviewId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-05",
          author = "Yooonjeong",
          description = "리뷰 CRUD 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/533"
      )
  })
  @Operation(
      summary = "대리인 리뷰 목록 조회",
      description = """
        ### 요청 파라미터 (`ReviewFilteredRequest`)
        - `agentId` (UUID, optional): 특정 대리인의 리뷰 조회 시 대리인 ID
        - `pageNumber` (int, default = 1): 페이지 번호 (1부터 시작)
        - `pageSize` (int, default = 10): 페이지당 데이터 개수 (최대 50)
        - `sortField` (ReviewSortField, default = CREATED_DATE): 정렬 기준 필드 (CREATED_DATE, RATING)
        - `sortDirection` (Sort.Direction, default = DESC): 정렬 방향 (ASC, DESC)

        ### 응답 데이터 (`Page<ReviewFilteredResponse>`)
        - `content` (List):
            - `reviewId` (UUID): 리뷰 ID
            - `concertTitle` (String): 공연 제목
            - `rating` (float): 별점
            - `comment` (String): 리뷰 내용
            - `reviewImgList` (List<ReviewImgResponse>):
                - `reviewImgId` (UUID): 이미지 ID
                - `reviewImgUrl` (String): 이미지 URL
            - `createdDate` (LocalDateTime): 작성 일시
        - `pageable`, `totalPages`, `totalElements`, `number`, `size`, `first`, `last`: 기본 페이징 정보

        ### 사용 방법
        - 특정 대리인의 리뷰 목록을 페이징 형태로 조회할 때 사용합니다.

        ### 유의 사항
        - `pageNumber`는 1부터 시작합니다.
        - `sortField` 값은 `ReviewSortField` Enum에서 허용된 필드만 가능합니다. (CREATED_DATE, RATING)
        
        ### 예외 처리
        - `MEMBER_NOT_FOUND` (400): "회원을 찾을 수 없습니다."
        - `INVALID_MEMBER_TYPE` (400): "잘못된 회원 자격입니다."
        """
  )
  ResponseEntity<Page<ReviewFilteredResponse>> getReviewsByAgent(ReviewFilteredRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-05",
          author = "Yooonjeong",
          description = "리뷰 CRUD 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/533"
      ),
      @ApiChangeLog(
          date = "2025-11-21",
          author = "Yooonjeong",
          description = "성공한 티켓팅에만 리뷰 작성 가능 검증 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/640"
      )
  })
  @Operation(
      summary = "리뷰 생성",
      description = """
        ### 요청 파라미터 (multipart/form-data)
        - `fulfillmentFormId` (UUID, required): 리뷰 대상 성공양식 ID
        - `rating` (Float, required): 별점 (0.0 이상, 5.0 이하)
        - `comment` (String, required): 리뷰 내용 (10자 이상, 300자 이하)
        - `reviewImgList` (List<MultipartFile>, optional): 리뷰 이미지 파일 목록 (최대 3개)

        ### 응답 데이터
        - `UUID`: 생성된 리뷰 ID

        ### 사용 방법
        1. 로그인된 사용자(의뢰인)가 멀티파트 폼으로 요청합니다. 서버는 인증 주체(`CustomOAuth2User`)에서 `member`를 사용합니다.
        2. 서버는 성공양식을 조회/검증 후 리뷰를 생성하고, (첨부 시) 이미지를 업로드합니다.
        3. 성공 시 생성된 `reviewId`(UUID)를 본문으로 반환합니다.

        ### 유의 사항
        - 이미지 첨부는 최대 3개까지만 허용됩니다.
        - 파일의 확장자는 jpg, jpeg, png, JPG, JPEG, PNG 만 가능합니다.
        - 필수 검증:
          - `fulfillmentFormId`: null 불가
          - `rating`: 0.0 이상 5.0 이하
          - `comment`: 10~300자
        - 권한 검증: 성공양식의 의뢰인과 로그인 사용자가 일치해야 합니다.
        - 동일 성공양식에 대한 리뷰가 이미 존재하면 생성할 수 없습니다.
        - 파일 업로드 중 오류가 발생하면 업로드된 파일은 안전하게 롤백됩니다.
        - **Swagger에서는 파일 없이 요청하면 400 오류가 발생할 수 있습니다. 실제 요청 시 이미지가 없다면 reviewImgList 필드는 아예 포함하지 말고 요청해 주세요.**

        ### 예외 처리
        - `FULFILLMENT_FORM_NOT_FOUND` (400): "성공양식 조회에 실패했습니다."
        - `NO_AUTH_TO_REVIEW` (403): "해당 성공양식에 대한 리뷰를 작성할 권한이 없습니다."
        - `CANNOT_REVIEW_NOT_SUCCEEDED_FORM` (400): "성공한 티켓팅에 대해서만 리뷰를 작성할 수 있습니다."
        - `REVIEW_ALREADY_EXISTS` (409): "이미 해당 성공양식에 대한 리뷰가 존재합니다."
        - `IMAGE_UPLOAD_LIMIT_EXCEEDED` (400): "리뷰 이미지는 최대 3개까지 등록 가능합니다."
        - `FILE_UPLOAD_ERROR` (500): "파일 업로드 중 오류가 발생했습니다."
        """
  )
  ResponseEntity<UUID> createReview(ReviewRequest request, CustomOAuth2User customOAuth2User);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-05",
          author = "Yooonjeong",
          description = "리뷰 CRUD 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/533"
      )
  })
  @Operation(
      summary = "리뷰 수정",
      description = """
        ### 요청 파라미터 (multipart/form-data)
        - `review-id` (UUID, path, required): 수정할 리뷰 ID
        - `rating` (Float, optional): 별점 (0.0 이상, 5.0 이하)
        - `comment` (String, optional): 리뷰 내용 (10자 이상, 300자 이하)
        - `deleteImgIdList` (List<UUID>, optional): 삭제할 기존 이미지 ID 목록
        - `newReviewImgList` (List<MultipartFile>, optional): 새로 추가할 이미지 파일 목록

        ### 응답 데이터
        - 본문 없음 (HTTP 200 OK)

        ### 사용 방법
        1. 작성자 본인만 수정할 수 있습니다.
        2. 리뷰 작성일로부터 **1개월 이내**(타임존: `Asia/Seoul`)에만 수정할 수 있습니다.
        3. 이미지 규칙:
           - 최종 보유 이미지 수는 **최대 3장**이어야 합니다.
           - 서버는 `현재 보유 수 - 삭제 예정 수 + 추가 예정 수 ≤ 3`을 검증합니다.
           - `deleteImgIdList`가 있으면 해당 이미지를 S3와 DB에서 삭제합니다.
           - `newReviewImgList`가 있으면 S3에 업로드 후 리뷰에 추가합니다.

        ### 유의 사항
        - 부분 수정이 가능하며 전달하지 않은 필드는 기존 값이 유지됩니다.
        - 파일의 확장자는 jpg, jpeg, png, JPG, JPEG, PNG 만 가능합니다.
        - **Swagger에서는 파일 없이 요청하면 400 오류가 발생할 수 있습니다. 실제 요청 시 이미지가 없다면 newReviewImgList 필드는 아예 포함하지 말고 요청해 주세요.**

        ### 예외 처리
        - `REVIEW_NOT_FOUND` (404): "해당 리뷰를 찾을 수 없습니다."
        - `NO_AUTH_TO_EDIT_REVIEW` (403): "해당 리뷰에 대한 수정/삭제 권한이 없습니다."
        - `REVIEW_EDIT_PERIOD_EXPIRED` (400): "리뷰 수정 가능 기간이 지났습니다."
        - `IMAGE_UPLOAD_LIMIT_EXCEEDED` (400): "리뷰 이미지는 최대 3개까지 등록 가능합니다."
        - `FILE_UPLOAD_ERROR` (500): "파일 업로드 중 오류가 발생했습니다."
        """
  )
  ResponseEntity<Void> updateReview(UUID reviewId, ReviewEditRequest request, CustomOAuth2User customOAuth2User);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-05",
          author = "Yooonjeong",
          description = "리뷰 CRUD 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/533"
      )
  })
  @Operation(
      summary = "대리인 댓글 작성/수정",
      description = """
        ### 요청 파라미터 (form-data)
        - `review-id` (UUID, path, required): 댓글을 작성할 리뷰 ID
        - `comment` (String, required): 댓글 내용 (1~300자)

        ### 응답 데이터
        - 본문 없음 (HTTP 200 OK)

        ### 사용 방법
        - 대리인은 자신에게 달린 리뷰에 댓글을 작성하거나 수정할 수 있습니다.
        - 댓글이 존재하지 않으면 새로 생성되고, 이미 존재하면 기존 내용이 수정됩니다.
        - 클라이언트는 별도의 수정 API 호출 없이 동일한 엔드포인트(`/api/review/{review-id}/comment`)를 사용하면 됩니다.

        ### 유의 사항
        - 댓글 내용은 300자 이하로 작성해야 합니다.
        - 리뷰가 존재하지 않거나, 해당 대리인이 아닌 사용자가 요청할 경우 권한 예외가 발생합니다.

        ### 예외 처리
        - `REVIEW_NOT_FOUND` (404): "해당 리뷰를 찾을 수 없습니다."
        - `NO_AUTH_TO_REVIEW_COMMENT` (403): "해당 리뷰에 대한 댓글 작성 권한이 없습니다."
        """
  )
  ResponseEntity<Void> addAgentComment(UUID reviewId, AgentCommentRequest request, CustomOAuth2User customOAuth2User);
}
