package com.ticketmate.backend.admin.application.validator;

import com.ticketmate.backend.admin.application.dto.request.TicketOpenDateRequest;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TicketOpenDateValidator {

  private final List<TicketOpenDateRequest> requestList;

  private TicketOpenDateValidator(List<TicketOpenDateRequest> requestList) {
    if (CommonUtil.nullOrEmpty(requestList)) {
      throw new CustomException(ErrorCode.INVALID_TICKET_OPEN_DATE);
    }
    this.requestList = requestList;
  }

  public static TicketOpenDateValidator of(List<TicketOpenDateRequest> requestList) {
    return new TicketOpenDateValidator(requestList);
  }

  /**
   * 선예매 오픈일 데이터가 최대 1개인지 검증
   */
  public TicketOpenDateValidator singlePreOpen() {
    long count = requestList.stream()
        .filter(request -> request.getTicketOpenType().equals(TicketOpenType.PRE_OPEN))
        .count();
    if (count > 1) {
      log.error("선예매 오픈일 데이터가 여러 개 요청되었습니다. 개수: {}", count);
      throw new CustomException(ErrorCode.PRE_OPEN_COUNT_EXCEED);
    }
    return this;
  }

  /**
   * 일반예매 오픈일 데이터가 최대 1개인지 검증
   */
  public TicketOpenDateValidator singleGeneralOpen() {
    long count = requestList.stream()
        .filter(request -> request.getTicketOpenType().equals(TicketOpenType.GENERAL_OPEN))
        .count();
    if (count > 1) {
      log.error("일반예매 오픈일 데이터가 여러 개 요청되었습니다. 개수: {}", count);
      throw new CustomException(ErrorCode.GENERAL_OPEN_COUNT_EXCEED);
    }
    return this;
  }
}
