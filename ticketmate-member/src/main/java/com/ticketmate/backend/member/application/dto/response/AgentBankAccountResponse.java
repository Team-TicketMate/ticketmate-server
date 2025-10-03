package com.ticketmate.backend.member.application.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AgentBankAccountResponse {
  private UUID agentBankAccountId;  // 계좌 PK
  private String agentAccountNumber;  // 계좌 번호
  private String bankName;  // 은행 이름
  private boolean primaryAccount;  // 대표계좌 여부
  private String accountHolder;  // 예금주

  @Builder
  public AgentBankAccountResponse(UUID agentBankAccountId, String agentAccountNumber, String bankName, boolean primaryAccount, String accountHolder) {
    this.agentBankAccountId = agentBankAccountId;
    this.agentAccountNumber = agentAccountNumber;
    this.bankName = bankName;
    this.primaryAccount = primaryAccount;
    this.accountHolder = accountHolder;
  }
}
