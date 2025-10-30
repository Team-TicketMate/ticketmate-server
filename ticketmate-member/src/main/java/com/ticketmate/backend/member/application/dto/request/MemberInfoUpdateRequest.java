package com.ticketmate.backend.member.application.dto.request;

import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.NICKNAME_MAX_LENGTH;
import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.NICKNAME_MIN_LENGTH;

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

  @Size(min = NICKNAME_MIN_LENGTH, max = NICKNAME_MAX_LENGTH, message = "닉네임은 최소2자 최대12까지 입력 가능합니다.")
  private String nickname;

  private MultipartFile profileImg;

  @Size(max = 50, message = "한줄소개는 최대 50자까지 작성 가능합니다")
  private String introduction;
}
