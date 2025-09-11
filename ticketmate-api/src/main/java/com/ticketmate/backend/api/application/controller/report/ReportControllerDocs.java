package com.ticketmate.backend.api.application.controller.report;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.report.application.dto.request.ReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface ReportControllerDocs {
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-09-02",
          author = "Yooonjeong",
          description = "사용자 신고 CRUD 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/486"
      )
  })
  @Operation(
      summary = "사용자 신고",
      description = """
          ### 요청 파라미터
          - `reportedUserId` (UUID, required): 신고 대상 사용자 ID
          - `reason` (ReportReason, required): 신고 사유 (ReportReason Enum 값)
          - `description` (String, optional): 신고 상세 설명
          
          ### 응답 데이터
          - 본 API는 응답 본문을 반환하지 않으며, 성공 시 **HTTP 201 (Created)** 상태 코드만 반환합니다.
          
          ### 사용 방법
          1. 로그인한 사용자가 특정 사용자를 신고할 때 사용합니다.
          2. 클라이언트는 JSON 형식으로 `reportedUserId`, `reason`, `description`을 담아 요청합니다.
          3. 서버는 신고자를 인증 정보(`AuthenticationPrincipal`)에서 확인한 후 신고 내용을 저장합니다.
          4. 저장이 성공하면 201 Created 상태 코드를 반환합니다.
          
          ### 유의 사항
          - `reportedUserId`와 `reason` 값은 필수입니다.
          - `description`은 선택값으로, 필요 시 신고 상세 내용을 작성할 수 있습니다.
          - 신고자는 반드시 로그인된 사용자여야 하며, 인증 정보가 없으면 접근이 불가능합니다.
          - 기획에 따라 ReportReason 값이 변경될 예정입니다.
          
          ### 예외 처리
          - `MEMBER_NOT_FOUND` (HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."): 신고자 ID 또는 신고 대상 사용자 ID가 DB에 존재하지 않을 경우 발생
          - `CANNOT_REPORT_SELF` (HttpStatus.BAD_REQUEST, "자기 자신을 신고할 수 없습니다."): 자신에 대한 ID로 요청했을 경우 발생
          """
  )

  ResponseEntity<Void> createReport(CustomOAuth2User customOAuth2User, ReportRequest request);
}
