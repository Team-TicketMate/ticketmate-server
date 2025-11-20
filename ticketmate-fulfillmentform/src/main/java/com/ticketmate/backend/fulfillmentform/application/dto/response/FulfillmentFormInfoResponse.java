package com.ticketmate.backend.fulfillmentform.application.dto.response;

import com.ticketmate.backend.fulfillmentform.core.constant.FulfillmentFormStatus;
import com.ticketmate.backend.member.application.dto.response.AgentBankAccountResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentFormInfoResponse {

  private UUID fulfillmentFormId;
  private List<FulfillmentFormImgResponse> fulfillmentFormImgUrlList = new ArrayList<>();
  private String particularMemo;
  private FulfillmentFormStatus fulfillmentFormStatus;
  private AgentBankAccountResponse agentBankAccount;
  private LocalDateTime createDate;
}
