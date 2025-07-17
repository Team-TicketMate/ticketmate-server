package com.ticketmate.backend.domain.sms.service;

public interface SmsService {

  /**
   * 지정한 번호로 문자 메시지 전송
   *
   * @param to      수신번호 (ex: "01012345678")
   * @param content 전송할 메시지 내용
   */
  void sendSms(String to, String content);

}
