package com.ticketmate.backend.member.application.dto.response;

import com.ticketmate.backend.member.core.constant.BankCode;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentBankAccountResponse {
  private UUID agentBankAccountId;  // 계좌 PK
  private String agentAccountNumber;  // 계좌 번호
  private BankCode bankCode;  // 은행 정보
  private boolean primaryAccount;  // 대표계좌 여부
  private String accountHolder;  // 예금주
}
