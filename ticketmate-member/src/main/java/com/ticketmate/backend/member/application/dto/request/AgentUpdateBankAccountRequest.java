package com.ticketmate.backend.member.application.dto.request;

import com.ticketmate.backend.member.core.constant.BankCode;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentUpdateBankAccountRequest {
  private BankCode bankCode;  // 은행 정보

  @Size(max = 20, message = "accountHolder는 최대 20자 입력 가능합니다")
  @Pattern(regexp = ".*\\S.*", message = "accountHolder가 비어있습니다")
  private String accountHolder;  // 예금주

  @Pattern(regexp = "^[0-9]{11,16}$", message = "accountNumber는 숫자 11~16자리여야 하고 '-' 문자가 없어야 합니다.")
  private String accountNumber;  // "-" 제거된 계좌번호
}
