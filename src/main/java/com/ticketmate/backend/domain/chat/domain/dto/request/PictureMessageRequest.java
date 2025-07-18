package com.ticketmate.backend.domain.chat.domain.dto.request;

import com.ticketmate.backend.domain.chat.domain.constant.ChatMessageType;
import com.ticketmate.backend.global.constant.ChatConstants;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public final class PictureMessageRequest implements ChatMessageRequest{
    @NotNull(message = "채팅을 전송할 이미지를 업로드하세요.")
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
