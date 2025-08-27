package com.ticketmate.backend.admin.sms.application.service;

import com.ticketmate.backend.admin.sms.application.dto.response.CoolSmsBalanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsAdminService {

  private final DefaultMessageService messageService;

  /**
   * CoolSMS 잔액 조회
   */
  public CoolSmsBalanceResponse getBalance() {
    Balance balance = messageService.getBalance();
    return CoolSmsBalanceResponse.builder()
        .balance(balance.getBalance() != null ? balance.getBalance() : 0f)
        .point(balance.getPoint() != null ? balance.getPoint() : 0f)
        .build();
  }
}
