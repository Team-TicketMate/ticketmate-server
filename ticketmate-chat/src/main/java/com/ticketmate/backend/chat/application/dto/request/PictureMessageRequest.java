package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import com.ticketmate.backend.chat.infrastructure.constant.ChatConstants;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class PictureMessageRequest implements ChatMessageRequest {

  @NotNull
  @NotNullErrorCode(ErrorCode.CHAT_MESSAGE_PICTURE_LIST_EMPTY)
  private List<MultipartFile> chatMessagePictureList;

  @Override
  public ChatMessageType getType() {
    return ChatMessageType.PICTURE;
  }

  @Override
  public String toPreview() {
    return ChatConstants.MESSAGE_PICTURE_PREVIEW_FORMAT;
  }
}
