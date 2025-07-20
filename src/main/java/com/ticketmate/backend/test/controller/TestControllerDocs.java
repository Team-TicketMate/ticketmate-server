package com.ticketmate.backend.test.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.test.dto.request.LoginRequest;
import com.ticketmate.backend.test.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;

public interface TestControllerDocs {

  @Operation(
      summary = "테스트 로그인",
      description = """
          
          이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          - **role** (String): 회원 권한 [필수]
          - **socialPlatform** (String): 소셜 플랫폼 [필수]
          - **memberType** (String): 의뢰인/대리자 (기본: 의뢰인)
          - **accountStatus** (String): 활성화/삭제 (기본: 활성화)
          - **isFirstLogin** (Boolean): 첫 로그인 여부
          
          ### 유의사항
          - 개발자의 편의를 위한 소셜 로그인 회원가입/로그인 메서드입니다
          - 스웨거에서 테스트 용도로만 사용해야하며, 엑세스 토큰만 제공됩니다.
          - `ROLE_TEST`, `ROLE_TEST_ADMIN`만 선택 가능합니다
          - username을 입력하지 않을 시 임의의 사용자가 생성됩니다
          """
  )
  ResponseEntity<LoginResponse> socialLogin(LoginRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-17",
          author = "Chuseok22",
          description = "회원 Mock 데이터 예외처리 로직 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/315"
      ),
      @ApiChangeLog(
          date = "2025-07-15",
          author = "Yooonjeong",
          description = "대리인 Mock 데이터 Summary 생성 및 랜덤 공연 수락 설정 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/417"
      ),
      @ApiChangeLog(
          date = "2025-07-20",
          author = "Yooonjeong",
          description = "대리인 Mock 데이터 Portfolio 및 Embedding 생성 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/376"
      )
  })
  @Operation(
      summary = "회원 Mock 데이터 생성",
      description = """
          ### ✅ 요청 파라미터
          - `count` (int): 생성할 Mock 회원 수 (1 이상 필수)
          
          ### 🔄 응답 데이터
          - HTTP 200 OK
          
          ### 🛠️ 사용 방법
          - 테스트 환경에서 회원 데이터가 필요할 때 사용하는 API입니다.
          - 요청 시 `count`만큼의 Mock 회원 데이터를 생성하여 DB에 저장합니다.
          - 내부적으로 멀티스레드로 처리되므로 빠른 속도로 대량 데이터를 생성할 수 있습니다.
          
          ### ⚠️ 유의 사항
          - `count`가 1 미만일 경우 기본값 1로 처리됩니다.
          - 생성된 회원 정보는 랜덤 데이터 기반이며 실제 유저 데이터가 아닙니다.
          - 해당 API는 운영 환경에서 사용하지 않도록 주의해야 합니다.
          - **DB에 콘서트 데이터가 최소 1개 이상 존재해야 합니다.**
          - 대리인 및 의뢰인 정보가 함께 생성되며, 대리인 생성 시 아래 내용이 함께 추가됩니다.
            - 해당 대리인의 활동 정보 랜덤값으로 추가
            - DB에 존재하는 랜덤 공연에 대해 수락 ON 설정
            - 대리인과 연결된 수락된 Portfolio 생성
            - 대리인 임베딩값 저장
          
          ### ❌ 예외 처리
          - `INTERNAL_SERVER_ERROR (500)`: 회원 Mock 데이터 저장 중 예기치 못한 오류가 발생한 경우
          - `INVALID_MEMBER_ROLE_REQUEST (400)`: 테스트 전용 Role(ROLE_TEST, ROLE_TEST_ADMIN) 외의 값이 요청될 경우
          """
  )
  CompletableFuture<ResponseEntity<String>> generateMockMembers(int count);

  @Operation(
      summary = "테스트 회원 삭제",
      description = """
          
          이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          `없음`
          
          ### 유의사항
          - 데이터베이스에 저장되어있는 모든 테스트 유저를 삭제합니다.
          
          """
  )
  ResponseEntity<Void> deleteTestMember();

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-17",
          author = "Chuseok22",
          description = "공연장 Mock 데이터 예외처리 로직 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/315"
      )
  })
  @Operation(
      summary = "테스트 공연장 데이터 추가",
      description = """
          
          이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          - **count** (Integer): 추가할 Mock 데이터 개수 (선택)
          
          ### 유의사항
          - 생성되는 공연장 Mock 데이터 기본값은 30개입니다
          - 중복 데이터 생성에 따라 사용자가 원하는 개수보다 적은 데이터가 저장될 수 있습니다
          
          """
  )
  CompletableFuture<ResponseEntity<String>> createConcertHallMockData(Integer count);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-17",
          author = "Chuseok22",
          description = "공연 Mock 데이터 예외처리 로직 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/315"
      )
  })
  @Operation(
      summary = "테스트 공연 데이터 추가",
      description = """
          
          이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          - **count** (Integer): 추가할 Mock 데이터 개수 (선택)
          
          ### 유의사항
          - 생성되는 공연 Mock 데이터 기본값은 30개입니다
          - 중복 데이터 생성에 따라 사용자가 원하는 개수보다 적은 데이터가 저장될 수 있습니다
          
          """
  )
  CompletableFuture<ResponseEntity<String>> createConcertMockData(Integer count);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-17",
          author = "Chuseok22",
          description = "신청서 Mock 데이터 예외처리 로직 추가 & 배치, 멀티스레드 기능 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/315"
      )
  })
  @Operation(
      summary = "테스트 신청서 데이터 추가",
      description = """
          
          이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          - **count** (Integer): 추가할 Mock 데이터 개수 (선택)
          
          ### 유의사항
          - 생성되는 신청서 Mock 데이터 기본값은 30개입니다
          - 중복 데이터 생성에 따라 사용자가 원하는 개수보다 적은 데이터가 저장될 수 있습니다
          """
  )
  CompletableFuture<ResponseEntity<String>> createApplicationFormMockData(Integer count);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-17",
          author = "Chuseok22",
          description = "포트폴리오 Mock 데이터 예외처리 로직 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/315"
      )
  })
  @Operation(
      summary = "포트폴리오 Mock 데이터 비동기 생성",
      description = """
          ### 요청 파라미터
          - `count` (int): 생성할 포트폴리오 Mock 데이터의 개수. 예: 10
          
          ### 응답 데이터
          - 본 API는 반환값이 없으며, 정상적으로 요청이 처리된 경우 HTTP 200 OK 상태 코드가 반환됩니다.
          
          ### 사용 방법 & 유의 사항
          - 이 API는 테스트나 개발 목적으로 포트폴리오 데이터를 대량으로 생성할 때 사용됩니다.
          - 내부적으로 멀티스레딩을 사용하여 비동기 방식으로 데이터를 생성하며, 모든 작업이 완료된 후 일괄 저장됩니다.
          - Portfolio 객체는 무작위로 생성되며, 포트폴리오 설명, 클라이언트 회원 정보, 포트폴리오 유형, 이미지 리스트를 포함합니다.
          - 생성된 포트폴리오는 실제 서비스와 무관한 테스트용 데이터입니다.
          - 동시에 다량의 데이터를 생성하므로 서버 부하에 유의해야 하며, 운영 환경에서는 사용하지 않는 것을 권장합니다.
          
          ### 예외 처리
          - `INTERNAL_SERVER_ERROR (500)`: 포트폴리오 데이터 생성 또는 저장 중 서버 내부 오류가 발생한 경우
          """
  )
  CompletableFuture<ResponseEntity<String>> createPortfolioMockData(Integer count);
}
