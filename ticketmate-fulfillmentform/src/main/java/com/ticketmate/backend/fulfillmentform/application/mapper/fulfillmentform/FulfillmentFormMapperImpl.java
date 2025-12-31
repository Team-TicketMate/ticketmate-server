package com.ticketmate.backend.fulfillmentform.application.mapper.fulfillmentform;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.fulfillmentform.application.dto.fulfillmentform.response.FulfillmentFormImgResponse;
import com.ticketmate.backend.fulfillmentform.application.dto.fulfillmentform.response.FulfillmentFormInfoResponse;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentFormImg;
import com.ticketmate.backend.member.application.mapper.AgentBankAccountMapper;
import com.ticketmate.backend.storage.core.service.StorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FulfillmentFormMapperImpl implements FulfillmentFormMapper {

  private final AgentBankAccountMapper mapper;
  private final StorageService storageService;

  @Override
  public FulfillmentFormInfoResponse toFulfillmentFormResponse(FulfillmentForm fulfillmentForm) {
    return new FulfillmentFormInfoResponse(
      fulfillmentForm.getFulfillmentFormId(),
      toFulfillmentFormImgList(fulfillmentForm.getSuccessTicketingStoredPathList()),
      fulfillmentForm.getParticularMemo(),
      fulfillmentForm.getFulfillmentFormStatus(),
      mapper.toAgentBankAccountResponse(fulfillmentForm.getAgentBankAccount()),
      TimeUtil.toLocalDateTime(fulfillmentForm.getCreatedDate())
    );
  }

  private List<FulfillmentFormImgResponse> toFulfillmentFormImgList(List<FulfillmentFormImg> fulfillmentFormImgList) {
    if (CommonUtil.nullOrEmpty(fulfillmentFormImgList)) {
      return List.of();
    }
    return fulfillmentFormImgList.stream()
      .filter(img -> !CommonUtil.nvl(img.getStoredPath(), "").isEmpty())
      .map(img -> new FulfillmentFormImgResponse(
        img.getFulfillmentFormImgId(),
        storageService.generatePublicUrl(img.getStoredPath())))
      .toList();
  }
}
