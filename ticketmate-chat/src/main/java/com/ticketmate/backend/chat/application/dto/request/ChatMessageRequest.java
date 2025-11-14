package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;

public sealed interface ChatMessageRequest permits TextMessageRequest, PictureMessageRequest, FulfillmentFormMessageRequest {

  /**
   * 사진을 보냈는지, 텍스트를 보냈는지에 대한 타입을 반환하는 메서드
   */
  ChatMessageType getType();

  /**
   * 채팅리스트 페이지 및 추후 확장을 고려한 미리보기 전용 메서드
   * (ex. 사진업로드시 -> 채팅방 리스트에서는 이미지를 보여주는것이 아닌 "사진을 보냈습니다.")
   */
  String toPreview();
}
