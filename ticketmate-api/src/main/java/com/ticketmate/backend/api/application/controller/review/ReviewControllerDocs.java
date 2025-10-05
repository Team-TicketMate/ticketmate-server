package com.ticketmate.backend.api.application.controller.review;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.review.application.dto.request.ReviewEditRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewRequest;
import com.ticketmate.backend.review.application.dto.response.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

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

        ### 응답 데이터 (`ReviewResponse`)
        - `reviewId` (UUID): 리뷰 ID
        - `concertTitle` (String): 공연 제목
        - `rating` (float): 별점
        - `comment` (String): 리뷰 내용
        - `reviewImgList` (List<ReviewImgResponse>): 리뷰 이미지 목록
            - `reviewImgId` (UUID): 리뷰 이미지 ID
            - `reviewImgUrl` (String): 리뷰 이미지 접근 URL
        - `createdDate` (LocalDateTime): 리뷰 생성 일시

        ### 사용 방법
        - 경로 변수 `review-id`를 전달하여 단건 조회합니다.

        ### 유의 사항
        - 존재하지 않는 리뷰 ID로 요청 시 예외가 발생합니다.

        ### 예외 처리
        - `REVIEW_NOT_FOUND` (404): "해당 리뷰를 찾을 수 없습니다."
        """
  )
  ResponseEntity<ReviewResponse> getReview(UUID reviewId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-05",
          author = "Yooonjeong",
          description = "리뷰 CRUD 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/533"
      )
  })
  @Operation(
      summary = "리뷰 생성",
      description = """
        ### 요청 파라미터 (multipart/form-data)
        - `applicationFormId` (UUID, required): 리뷰 대상 신청서 ID
        - `rating` (Float, required): 별점 (0.0 이상, 5.0 이하)
        - `comment` (String, required): 리뷰 내용 (10자 이상, 300자 이하)
        - `reviewImgList` (List<MultipartFile>, optional): 리뷰 이미지 파일 목록 (최대 3개)

        ### 응답 데이터
        - `UUID`: 생성된 리뷰 ID

        ### 사용 방법
        1. 로그인된 사용자(의뢰인)가 멀티파트 폼으로 요청합니다. 서버는 인증 주체(`CustomOAuth2User`)에서 `member`를 사용합니다.
        2. 서버는 신청서를 조회/검증 후 리뷰를 생성하고, (첨부 시) 이미지를 업로드합니다.
        3. 성공 시 생성된 `reviewId`(UUID)를 본문으로 반환합니다.

        ### 유의 사항
        - 이미지 첨부는 최대 3개까지만 허용됩니다.
        - 파일의 확장자는 jpg, jpeg, png, JPG, JPEG, PNG 만 가능합니다.
        - 필수 검증:
          - `applicationFormId`: null 불가
          - `rating`: 0.0 이상 5.0 이하
          - `comment`: 10~300자
        - 권한 검증: 신청서의 의뢰인과 로그인 사용자가 일치해야 합니다.
        - 동일 신청서에 대한 리뷰가 이미 존재하면 생성할 수 없습니다.
        - 파일 업로드 중 오류가 발생하면 업로드된 파일은 안전하게 롤백됩니다.
        - **Swagger에서는 파일 없이 요청하면 400 오류가 발생할 수 있습니다. 실제 요청 시 이미지가 없다면 reviewImgList 필드는 아예 포함하지 말고 요청해 주세요.**

        ### 예외 처리
        - `APPLICATION_FORM_NOT_FOUND` (400): "대리 티켓팅 신청서를 찾을 수 없습니다."
        - `NO_AUTH_TO_REVIEW` (403): "해당 신청서에 대한 리뷰를 작성할 권한이 없습니다."
        - `REVIEW_ALREADY_EXISTS` (409): "이미 해당 신청서에 대한 리뷰가 존재합니다."
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
        - `NO_AUTH_TO_REVIEW` (403): "해당 신청서에 대한 리뷰를 작성할 권한이 없습니다."
        - `REVIEW_EDIT_PERIOD_EXPIRED` (400): "리뷰 수정 가능 기간이 지났습니다."
        - `IMAGE_UPLOAD_LIMIT_EXCEEDED` (400): "리뷰 이미지는 최대 3개까지 등록 가능합니다."
        - `FILE_UPLOAD_ERROR` (500): "파일 업로드 중 오류가 발생했습니다."
        """
  )
  ResponseEntity<Void> updateReview(UUID reviewId, ReviewEditRequest request, CustomOAuth2User customOAuth2User);
}
