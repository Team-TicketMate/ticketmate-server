package com.ticketmate.backend.member.application.dto.request;

import static com.ticketmate.backend.common.core.constant.ValidationConstants.Member.NICKNAME_MAX_LENGTH;
import static com.ticketmate.backend.common.core.constant.ValidationConstants.Member.NICKNAME_MIN_LENGTH;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import jakarta.validation.constraints.Size;
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
public class MemberInfoUpdateRequest {

  @Size(min = NICKNAME_MIN_LENGTH, max = NICKNAME_MAX_LENGTH)
  @SizeErrorCode(ErrorCode.NICKNAME_LENGTH_INVALID)
  private String nickname;

  private MultipartFile profileImg;

  @Size(max = 50)
  @SizeErrorCode(ErrorCode.INTRODUCTION_TOO_LONG)
  private String introduction;
}
