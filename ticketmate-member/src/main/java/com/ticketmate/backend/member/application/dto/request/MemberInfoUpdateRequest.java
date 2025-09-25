package com.ticketmate.backend.member.application.dto.request;

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

  private String nickname;

  private MultipartFile profileImg;

  @Size(max = 50, message = "한줄소개는 최대 50자까지 작성 가능합니다")
  private String introduction;
}
