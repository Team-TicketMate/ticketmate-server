package com.ticketmate.backend.controller.chat.docs;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.chat.reqeust.ChatRoomFilteredRequest;
import com.ticketmate.backend.object.dto.chat.response.ChatRoomListResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ChatRoomControllerDocs {
    @Operation(
            summary = "채팅방 리스트업",
            description = """
                                        
                    이 API는 인증이 필요합니다.

                    ### 요청 파라미터
                    - **isPreOpen (PreOpenFilter)** : 선예매/일반예매 검색 카테고리 여부 [필수]
                    - **pageNum (INTEGER)** : 요청 페이지 번호 [필수X]
                    - **searchKeyword (STRING)** : 검색키워드 [필수X]
                        
                    ### 반환값
                    - **roomId** : 채팅방 고유 PK
                    - **chatRoomName** : 채팅방 이름 (상대방 닉네임)
                    - **lastChatMessage** : 마지막 채팅 내용
                    - **lastChatSendTime** : 마지막 채팅 시간
                    - **messageCount** : 쌓인 채팅 갯수 (아직 보지 않은 채팅)
                    - **profileImg** : 상대방 프로필 이미지
                    - **concertImg** : 콘서트 썸네일 이미지
                    - **isPreOpen** : 선예매/일반예매 구분
                     
                    ### 사용 방법
                    `PreOpenFilter`
                    
                    ALL("선예매/일반예매 전체조회")
                
                    PRE_OPEN("선예매")
                
                    NORMAL("일반예매")
                               
                    ### 유의사항
                    - 페이징 처리는 페이지당 20개 데이터로 처리했습니다.
                    - 페이지 번호를 비우고 요청시 페이지 기본 '0' 으로 설정합니다. (첫페이지)
                    - 검색키워드가 없을 시 공백으로 설정합니다. (전체조회입니다.)                     
                    """
    )
    ResponseEntity <Page<ChatRoomListResponse>> getChatRoomList(CustomOAuth2User customOAuth2User, ChatRoomFilteredRequest request);
}
