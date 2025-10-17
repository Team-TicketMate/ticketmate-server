package com.ticketmate.backend.member.application.dto.request;

import com.ticketmate.backend.member.core.constant.BankCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AgentSaveBankAccountRequest {
  @NotNull(message = "은행을 선택해주세요.")
  private BankCode bankCode;  // 은행 코드

  @NotBlank(message = "예금주를 입력하세요.")
  @Size(max = 20, message = "예금주명은 최대 20자 입니다.")
  @Pattern(regexp = ".*\\S.*", message = "예금주명은 공백만으로 구성될 수 없습니다.")
  private String accountHolder;  // 예금주

  @NotBlank(message = "계좌번호를 입력해주세요.")
  @Pattern(regexp = "^[0-9]{11,16}$", message = "계좌번호는 숫자 11~16자리여야 하고 '-' 문자가 없어야 합니다.")
  private String accountNumber;  // "-" 제거된 계좌번호

  @NotNull(message = "대표계좌 유무 체크를 확인하세요.")
  private boolean primaryAccount;  // 대표계좌 유/무 (등록하면서 바로 설정)
}
