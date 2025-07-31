package com.ticketmate.backend.sms.application.service;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.sms.core.service.SmsService;
import com.ticketmate.backend.sms.infrastructure.properties.CoolSmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class CoolSmsService implements SmsService {

  private final DefaultMessageService messageService;
  private final CoolSmsProperties coolSmsProperties;

  /**
   * SMS 발송
   *
   * @param to      수신번호 (ex: "01012345678")
   * @param content 전송할 메시지 내용
   */
  @Override
  public void sendSms(String to, String content) {
    Message message = buildMessage(to, content);
    sendMessage(message);
  }

  /**
   * 메시지 모델 생성
   *
   * @param to      수신자
   * @param content 메시지 내용
   */
  private Message buildMessage(String to, String content) {
    Message message = new Message();
    message.setFrom(coolSmsProperties.from());
    message.setTo(to);
    message.setText(content);
    return message;
  }

  /**
   * 메시지 발송
   *
   * @param message 발송하려는 메시지
   */
  private void sendMessage(Message message) {
    try {
      messageService.send(message);
      log.debug("SMS 전송 성공");
    } catch (NurigoMessageNotReceivedException e) {
      log.error("SMS 전송 실패 - 메시지 접수 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.MESSAGE_NOT_RECEIVED);
    } catch (NurigoEmptyResponseException e) {
      log.error("SMS 전송 실패 - SMS 서버로부터의 빈 응답: {}", e.getMessage());
      throw new CustomException(ErrorCode.SMS_EMPTY_RESPONSE);
    } catch (NurigoUnknownException e) {
      log.error("SMS 전송 실패 - 알 수 없는 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.SMS_UNKNOWN_ERROR);
    } catch (Exception e) {
      log.error("SMS 발송 중 예외 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.SMS_SEND_ERROR);
    }
  }
}
