package com.ticketmate.backend.api.application.controller.fulfillmentform;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.fulfillmentform.application.dto.request.FulfillmentFormInfoRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.request.FulfillmentFormRejectRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.request.FulfillmentFormUpdateRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.response.FulfillmentFormInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface FulfillmentFormControllerDocs {

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2025-11-07",
      author = "mr6208",
      description = "대리인 성공양식 등록 기능 개발",
      issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/534"
    )
  })
  @Operation(
    summary = "성공양식 등록",
    description = """
      대리인이 티켓팅 성공 후 성공양식을 작성합니다.
                
      ### 요청 파라미터
      - `chat-room-id` : 현재 티켓팅이 진행된 채팅방의 ID
                
      ## FulfillmentFormInfoRequest
      - `fulfillmentFormImgList[List]` : 성공양식에 첨부할 사진(1~6개)
      - `particularMemo`[String]` : 상세설명 (10자~100자)
      - `agentBankAccountId`[UUID]` : 대리인 계좌 ID

      ### 1:1채팅방 플래그 전송 예시
       ```json
      {
        "messageId":"69168a85c7ab845b344146c1",
        "senderNickname":"닉변한닉네임임",
        "message":null,
        "sendDate":[2025,11,14,10,48,53],
        "profileUrl":"https://picsum.photos/640/350311a9233-9111-48ea-9157-e4d5d6bb291208",
        "mine":false,
        "chatMessageType":"FULFILLMENT_FORM",
        "referenceId":"4c6a12a8-c58a-4ecd-97f9-936aa8b439d8",
        "isRead":false
      }      
       ```
       
      ### 채팅방 `chatMessageType` 관련 반환값
      - 성공양식쪽을 설계하면서 채팅과도 연계되어있어 채팅 메시지 타입 내부에 필드값을 추가했습니다.
      - 기존 텍스트 메시지와 사진 메시지 뿐만이 아닌 성공양식이 등록,수락,거절,수정 됐는지데 대한 필드를 추가했습니다.
            
        `FULFILLMENT_FORM("성공양식 전송")`
        
        `ACCEPTED_FULFILLMENT_FORM("성공양식 수락")`
        
        `REJECTED_FULFILLMENT_FORM("성공양식 거절")`
        
        `UPDATE_FULFILLMENT_FORM("성공양식 수정")`
            
      - 해당 필드값은 성공양식의 상태가 변경될때마다 채팅방 내부에 위의 JSON 데이터 처럼 전송됩니다.
            
      ## 예시
       `chatMessageType":"FULFILLMENT_FORM` : 성공양식이 처음으로 등록되었을때
       `chatMessageType":"ACCEPTED_FULFILLMENT_FORM` : 성공양식이 수락되었을때
       `chatMessageType":"REJECTED_FULFILLMENT_FORM` : 성공양식이 거절되었을때
       `chatMessageType":"UPDATE_FULFILLMENT_FORM` : 성공양식이 수정되었을때       
       
      - 프론트는 해당 필드타입을 보고 적절하게 UI를 설계하면 될 것 같습니다.
            
      ### 유의 사항
      - 대리인 한정 티켓팅 성공 후 매칭된 의뢰인에게 성공했다는 정보를 전달하기 위해 양식을 작성하는 API입니다.   
      - 성공적으로 성공양식이 등록된 이후에는 채팅을 통해 대리인 -> 의뢰인에게 성공했다는 플래그 데이터를 자동으로 보내도록 설계했습니다.
      - 채팅방 내부 성공양식 조회를 간편하게 하기 위해서 채팅메시지에 필드를 추가했습니다. (referenceId)
      - 해당 필드는 특별한 상황(성공양식 같은 엔티티를 쉽게 조회할 수 있게 하기 위해 엔티티의 ID를 담고있음)에만 존재합니다.
      - 성공양식 등록 API는 1:1 채팅방 내부에 반드시 하나만 생성할 수 있습니다.
      """
  )
  ResponseEntity<UUID> saveFulfillmentFormInfo
    (CustomOAuth2User customOAuth2User,
      String chatRoomId,
      FulfillmentFormInfoRequest request
    );

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2025-11-07",
      author = "mr6208",
      description = "대리인/의뢰인 성공양식 조회 기능 개발",
      issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/534"
    )
  })
  @Operation(
    summary = "성공양식 조회",
    description = """
      대리인 및 의뢰인이 티켓팅 성공 후 작성한 성공양식을 상세 조회합니다.
                
      ### 요청 파라미터
      - `fulfillment-form-id` : 성공양식 ID
       
      ### 응답값
      - `fulfillmentFormId[UUID]` : 조회한 성공양식 ID
      - `fulfillmentFormImgUrlList[FulfillmentFormImgResponse]` : 성공양식 이미지 리스트
      - `particularMemo[String]` : 상세 메모
      - `agentBankAccount[AgentBankAccountResponse]` : 대리인 계좌정보
      - `createDate[LocalDateTime]` : 생성일자
            
      ### 반환 예시
       ```json
      {
        "fulfillmentFormId": "d2b0c058-c5ce-47a2-a4ce-85fa6ab92a34",
        "fulfillmentFormImgUrlList": [
          {
            "fulfillmentFormImgId": "01a8e5d0-eecc-4fbe-952e-951fe38758d7",
            "fulfillmentFormImgUrl": "https://ticketmate-storage.s3.ap-n12ortheast-2.amazonaws.com/fulfillment-form/20251112-9ee4241d-7aa9-44de-8311-98ac042ae1ec-스크린샷_2023-12-06_오전_12.48.02.png"
          },
          {
            "fulfillmentFormImgId": "6ac81af4-1996-4a1d-ae69-475200999251",
            "fulfillmentFormImgUrl": "https://ticketmate-storage.s3.ap-nort12heast-2.amazonaws.com/fulfillment-form/20251112-1ff6541250-7e1e-4183-b943-4e539a8e13a8-스크린샷_2025-09-08_오후_6.50.18.png"
          },
          {
            "fulfillmentFormImgId": "e8bcda92-e740-4711-9c2d-c9735259d0f5",
            "fulfillmentFormImgUrl": "https://ticketmate-storage.s3.ap-northeast-2.amazonaws.com/fulfillment-form/20251114-c530c01266-cf34-4b0d-8257-5acfc286706f-스크린샷_2025-10-20_오후_7.01.17.png"
          }
        ],
        "particularMemo": "",
        "agentBankAccount": {
          "agentBankAccountId": "49f15a90-45d4-49f6-b68d-d214d28ee717",
          "agentAccountNumber": "984743461787",
          "bankCode": "KAKAO_BANK",
          "primaryAccount": true,
          "accountHolder": "낑낑이"
        },
        "createDate": "2025-11-12T17:50:55"
      }      
       ```
       
      ### 유의 사항
      - 조회 API는 의뢰인 및 대리인 모두 사용 가능합니다.   
      """
  )
  ResponseEntity<FulfillmentFormInfoResponse> getFulfillmentFormInfo
    (CustomOAuth2User customOAuth2User,
      UUID fulfillmentFormId
    );

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2025-11-07",
      author = "mr6208",
      description = "의뢰인 성공양식 수락 기능 개발",
      issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/534"
    )
  })
  @Operation(
    summary = "성공양식 수락",
    description = """
      의뢰인이 성공양식을 본 후 수락하는 API입니다.
                
      ### 요청 파라미터
      - `fulfillment-form-id` : 성공양식 ID
       
      ### 응답값
        - API반환값은 따로 없지만 1:1채팅방 내부에 의뢰인이 수락했다는 플래그를 전송합니다.
            
      ### 1:1채팅방 플래그 전송 예시
       ```json
      {
        "messageId": "69168c90c7ab845b344146c2",
        "senderNickname": "닉변한닉네임인데그거임",
        "message": null,
        "sendDate": "2025-11-14T10:57:36",
        "profileUrl": "https://picsum.photos/640/148023959b09-f3c2-4d61-81125e-3db7d60c5734",
        "mine": true,
        "chatMessageType": "ACCEPTED_FULFILLMENT_FORM",
        "referenceId": "4c6a12a8-c58a-4ecd-97f9-936aa8b439d8",
        "isRead": false
      }      
       ```
       
      ### 유의 사항
      - 성공양식 수락/거절 API는 의뢰인만 사용 가능합니다.   
      - 성공양식이 수락된 이후에는 채팅을 통해 의뢰인 -> 대리인에게 수락했다는 플래그 데이터를 자동으로 보내도록 설계했습니다.
      - 의뢰인은 수락 -> 거절 상태의 변경은 불가하지만 거절 -> 수락 상태로의 변경은 가능합니다.
      """
  )
  ResponseEntity<Void> acceptFulfillmentForm
    (CustomOAuth2User customOAuth2User,
      UUID fulfillmentFormId
    );

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2025-11-07",
      author = "mr6208",
      description = "의뢰인 성공양식 거절 기능 개발",
      issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/534"
    )
  })
  @Operation(
    summary = "성공양식 거절",
    description = """
      의뢰인이 성공양식을 본 후 거절하는 API입니다.
                
      ### 요청 파라미터
      - `fulfillment-form-id` : 성공양식 ID
            
      ### 요청값
      - `rejectedMemo`(String) : 거절사유[필수X]
       
      ### 응답값
        - API반환값은 따로 없지만 1:1채팅방 내부에 의뢰인이 거절했다는 플래그를 전송합니다.
            
      ### 1:1채팅방 플래그 전송 예시
       ```json
      {
        "messageId": "69168c90c7ab845b344146c2",
        "senderNickname": "닉변한닉네임인데그거임",
        "message": "희망구역과 다릅니다.",
        "sendDate": "2025-11-14T10:57:36",
        "profileUrl": "https://picsum.photos/640/148023959b09-f3c2-4d61-81125e-3db7d60c5734",
        "mine": true,
        "chatMessageType": "REJECTED_FULFILLMENT_FORM",
        "referenceId": "4c6a12a8-c58a-4ecd-97f9-936aa8b439d8",
        "isRead": false
      }      
       ```
       
      ### 유의 사항
      - 성공양식 수락/거절 API는 의뢰인만 사용 가능합니다.   
      - 성공양식이 거절된 이후에는 채팅을 통해 의뢰인 -> 대리인에게 수락했다는 플래그 데이터를 자동으로 보내도록 설계했습니다.
      - 의뢰인은 수락 -> 거절 상태의 변경은 불가하지만 거절 -> 수락 상태로의 변경은 가능합니다.
      - 성공양식 수락과 가장 큰 차이점은 채팅 플래그 데이터의 `message`필드에 데이터가 들어간다는 점입니다.
        - 해당 데이터는 요청시에 받은 `rejectedMemo` 와 같습니다. 현재 성공양식 도메인에는 거절사유 필드가 없기 때문에 추후 프론트측에서 거절사유 UI를 설계할때 해당 필드를 매핑하면 될 것 같습니다.
      """
  )
  ResponseEntity<Void> rejectFulfillmentForm
    (CustomOAuth2User customOAuth2User,
      UUID fulfillmentFormId,
      FulfillmentFormRejectRequest request
    );

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2025-11-07",
      author = "mr6208",
      description = "의뢰인 성공양식 수정 기능 개발",
      issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/534"
    )
  })
  @Operation(
    summary = "성공양식 수정",
    description = """
      대리인이 사진이 작성한 성공양식을 수정하는 API입니다.
                
      ### 요청 파라미터
      - `fulfillment-form-id` : 성공양식 ID
            
      ### 요청값    
      - `deleteImgIdList`(List) : 삭제할 이미지 ID 리스트값 [필수X]
      - `newSuccessImgList`(List) : 새로 추가할 이미지 리스트 [필수X]
      - `agentBankAccountId`(UUID) : 수정할 계좌번호 ID [필수X]
      - `particularMemo`(String) : 수정할 상세설명 [필수X, 0~100자]
       
       
      ### 응답값
        - API반환값은 따로 없지만 1:1채팅방 내부에 의뢰인이 수정했다는 플래그를 전송합니다.
            
      ### 1:1채팅방 플래그 전송 예시
       ```json
      {
        "messageId": "69168c90c7ab845b344146c2",
        "senderNickname": "닉변한닉네임인데그거임",
        "message": "null",
        "sendDate": "2025-11-14T10:57:36",
        "profileUrl": "https://picsum.photos/640/148023959b09-f3c2-4d61-81125e-3db7d60c5734",
        "mine": true,
        "chatMessageType": "UPDATE_FULFILLMENT_FORM",
        "referenceId": "4c6a12a8-c58a-4ecd-97f9-936aa8b439d8",
        "isRead": false
      }      
       ```
       
      ### 유의 사항
      - 성공양식 수정 API는 대리인만 사용 가능합니다.   
      - 수정이 가능한 성공양식의 상태는 거절이 된 상태 혹은 수락이전상태 뿐 입니다(수락되고나서 수정X)
      - `deleteImgIdList` 및 `newSuccessImgList` 필드를 받은 후 서버내부에서 최종 이미지를 계산해 6장이 넘어간다면 예외를 터트리도록 설계했습니다.
      - 사진을 추가하고 싶지 않거나 삭제하고 싶지 않다면 위의 두 필드들을 추가하지 않으시면 됩니다.
      """
  )
  ResponseEntity<Void> updateFulfillmentForm
    (CustomOAuth2User customOAuth2User,
      UUID fulfillmentFormId,
      FulfillmentFormUpdateRequest request
    );
}
