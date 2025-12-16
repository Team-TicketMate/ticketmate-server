package com.ticketmate.backend.fulfillmentform.application.dto.request;

import static com.ticketmate.backend.fulfillmentform.infrastructure.constant.FulfillmentFormConstants.MAX_IMG_COUNT;
import static com.ticketmate.backend.fulfillmentform.infrastructure.constant.FulfillmentFormConstants.MAX_PARTICULAR_MEMO_LENGTH;

import jakarta.validation.constraints.NotNull;
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
public class FulfillmentFormInfoRequest {

  @Size(max = MAX_IMG_COUNT, message = "fulfillmentFormImgList는 최대 6개 등록 가능합니다.")
  private List<MultipartFile> fulfillmentFormImgList;

  @Size(max = MAX_PARTICULAR_MEMO_LENGTH, message = "particularMemo는 최대 100자 입력 가능합니다.")
  private String particularMemo;

  @NotNull(message = "agentBankAccountId가 비어있습니다")
  private UUID agentBankAccountId;
}
