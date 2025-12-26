package com.ticketmate.backend.fulfillmentform.application.dto.request;

import static com.ticketmate.backend.fulfillmentform.infrastructure.constant.FulfillmentFormConstants.MAX_PARTICULAR_MEMO_LENGTH;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FulfillmentFormUpdateRequest {

  private List<UUID> deleteImgIdList;  // 삭제하고 싶은 기존 사진 리스트
  
  private List<MultipartFile> newSuccessImgList;  // 추가로 등록하고 싶은 사진 리스트
  
  private UUID agentBankAccountId;  // 수정할 대리인 계좌번호 ID

  @Size(max = MAX_PARTICULAR_MEMO_LENGTH)
  @SizeErrorCode(ErrorCode.PARTICULAR_MEMO_TOO_LONG)
  private String particularMemo;  // 수정할 상세설명
}
