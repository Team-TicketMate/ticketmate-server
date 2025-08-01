package com.ticketmate.backend.api.application.controller.chat;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.chat.application.dto.request.ChatMessageFilteredRequest;
import com.ticketmate.backend.chat.application.dto.request.ChatRoomFilteredRequest;
import com.ticketmate.backend.chat.application.dto.request.ChatRoomRequest;
import com.ticketmate.backend.chat.application.dto.response.ChatMessageResponse;
import com.ticketmate.backend.chat.application.dto.response.ChatRoomListResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;

public interface ChatRoomControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-08-01",
          author = "Chuseok22",
          description = "채팅방 생성 API 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/444"
      )
  })
  @Operation(
      summary = "채팅방 생성",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **agentId (UUID)** : 대리인 PK [필수]
          - **clientId (UUID)** : 의뢰인 PK [필수]
          - **concertId UUID)** : 공연 PK [필수]
          - **ticketOpenType (String)** : 공연 타입 (선예매, 일반예매) [필수]
          
          ### 반환값
          - **chatRoomId** : 채팅방 고유 PK
          
          ### 사용 방법
          `TicketOpenType`
          
          PRE_OPEN("선예매")
          
          GENERAL_OPEN("일반예매")
          
          ### 유의사항
          - 이미 고유한 채팅방이 존재하는 경우 에러를 반환합니다
          """
  )
  ResponseEntity<String> generateChatRoom(
      CustomOAuth2User customOAuth2User,
      ChatRoomRequest request
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-24",
          author = "mr6208",
          description = "채팅방 리스트업 API 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/338"
      ),
      @ApiChangeLog(
          date = "2025-06-25",
          author = "mr6208",
          description = "채팅방 리스트업 API 응답 데이터 명세 변경",
          issueUrl = "Discord를 이용해 알게된 HotFix..."
      ),
      @ApiChangeLog(
          date = "2025-07-07",
          author = "Chuseok22",
          description = "SortField 정렬 필드 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/393"
      )
  })
  @Operation(
      summary = "채팅방 리스트업",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **ticketOpenType (TicketOpenType)** : 선예매/일반예매 검색 카테고리 여부 [필수X]
          - **pageNumber (INTEGER)** : 요청 페이지 번호 [필수X]
          - **searchKeyword (STRING)** : 검색키워드 [필수X]
          
          ### 반환값
          - **chatRoomId** : 채팅방 고유 PK
          - **chatRoomName** : 채팅방 이름 (상대방 닉네임)
          - **lastChatMessage** : 마지막 채팅 내용
          - **lastChatSendTime** : 마지막 채팅 시간
          - **profileUrl** : 상대방 프로필 이미지
          - **concertThumbnailUrl** : 콘서트 썸네일 이미지
          - **ticketOpenType** : 선예매/일반예매 구분
          - **unReadMessageCount** : 읽지 않은 메시지 카운트
          
          ### 사용 방법
          `TicketOpenType`
          
          PRE_OPEN("선예매")
          
          GENERAL_OPEN("일반예매")
          
          ### 유의사항
          - TicketOpenType파라미터를 공백 혹은 NULL로 요청 시 채팅방을 전체조회합니다.
          - 기본 페이징 처리는 페이지당 10개 데이터로 처리했습니다.
          - pageNumber는 기본 1페이지로 설정됩니다. (첫페이지)
          - 검색키워드가 없을 시 공백으로 설정합니다. (전체조회입니다.)
          """
  )
  ResponseEntity<Page<ChatRoomListResponse>> getChatRoomList(CustomOAuth2User customOAuth2User, ChatRoomFilteredRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-26",
          author = "mr6208",
          description = "채팅방 입장 DTO 필드 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/363"
      ),
      @ApiChangeLog(
          date = "2025-06-27",
          author = "mr6208",
          description = "명세 바뀐 DTO 필드명 추가 갱신",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/363"
      ),
      @ApiChangeLog(
          date = "2025-06-27",
          author = "mr6208",
          description = "채팅 메시지 페이지네이션",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/370"
      ),
      @ApiChangeLog(
          date = "2025-07-07",
          author = "Chuseok22",
          description = "SortField 정렬 필드 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/393"
      )
  })
  @Operation(
      summary = "채팅방 입장 및 채팅 메시지 페이지네이션",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **chat-room-id (String)** : 채팅방 고유 ID [필수]
          
          - ChatMessageFilteredRequest
            - **pageNumber (int)** : 요청할 페이지 번호 [필수X]
            - **pageSize (int)** : 요청할 페이지 사이즈 [필수X]
            - **sortField (String)** : 정렬 필드 [필수X]
            - **sortDirection (String)** : 정렬 방향 [필수X]
          
          ### 반환값 [LIST]
          - **chatRoomId** : 채팅방 고유 ID
          - **messageId** : 채팅 메시지 ID
          - **senderId** : 채팅 메시지를 보낸 사용자 ID
          - **senderNickname** : 채팅 메시지를 보낸 사용자 닉네임
          - **message** : 메시지 정보
          - **sendDate** : 채팅을 보낸 시간
          - **read** : 채팅의 읽음 여부
          - **profileUrl** : 채팅을 보낸 사용자의 프로필 사진
          - **mine** : 메시지를 보낸 사람의 유무 (자신의 메시지이면 true/상대방의 메시지이면 false)
          
          ### 유의사항
          - 채팅방 입장시 가장 최근에 전송된 메시지 20개를 페이지 형태로 반환합니다 (Slice)
          - pageNumber의 기본값은 '1' 입니다.
          - pageSize의 기본값은 '20' 입니다.
          - sortField: 'CREATED_DATE'
          - sortDirection: 'ASC', 'DESC'
          - 페이지네이션 파라미터를 Null 형태로 전송 할 경우, 기본값으로 pageNumger = 1, pageSize = 20, sortField = 'CREATED_DATE', sortDirection = 'DESC'로 설정되어 응답합니다.
          - 기존 Page타입과 달리 Slice를 이용해 클라이언트는 first, last 플래그 변수를 보고 무한 스크롤 구현이 가능합니다.
          - first 가 true일 경우 -> 첫번째 페이지
          - last 가 true일 경우 -> 마지막 페이지 (다음 데이터는 없음)
          """
  )
  ResponseEntity<Slice<ChatMessageResponse>> enterChatRoom(CustomOAuth2User customOAuth2User, String chatRoomId, ChatMessageFilteredRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-23",
          author = "mr6208",
          description = "채팅방 신청서 조회 API 설계",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/194"
      )
  })
  @Operation(
      summary = "채팅방 내부 신청서 조회",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **chat-room-id (String)** : 채팅방 고유 ID [필수]
          
          ### 반환 데이터
          - ApplicationFormFilteredResponse: 현재 채팅방에서 진행중인 단일 신청서 정보
            - applicationFormId: 신청서 PK
            - clientId: 의뢰인 PK
            - agentId: 대리인 PK
            - concertId: 콘서트 PK
            - openDate: 티켓 예매일
            - applicationFormDetailResponseList: 신청서 세부사항 리스트
            - applicationFormStatus: 신청서 상태
            - ticketOpenType: 선예매/일반예매 타입
          
          
          - applicationFormDetailResponseList: 현재 신청서에 적용된 N개의 회차들의 정보
            - performanceDate: 공연 일자
            - session: 회차
            - requestCount: 요청 매수
            - hopeAreaResponseList: 희망 구역 리스트
            - requirement: 요청 사항
          
          - hopeAreaResponseList: 회차당 희망구역 리스트
            - priority: 우선 순위 (1~10)
            - location: 위치 (예: A구역, B구역)
            - price: 가격
          
          ### 유의사항
          - 선예매/일반예매 및 공연 기준 모두 각각의 채팅방에 존재합니다.
          - 신청서의 정보에는 모든 **회차**가 포함됩니다. (1:N, 신청서 : 회차)
          """
  )
  ResponseEntity<ApplicationFormFilteredResponse> chatRoomApplicationFormInfo(CustomOAuth2User customOAuth2User, String chatRoomId);
}
