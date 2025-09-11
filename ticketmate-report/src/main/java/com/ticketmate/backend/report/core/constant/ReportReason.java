package com.ticketmate.backend.report.core.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@RequiredArgsConstructor
public enum ReportReason {
  INAPPROPRIATE_LANGUAGE("불건전한 언행 사용"),
  FAKE_LISTING("허위매물"),
  SPAM("스팸");

  private final String description;

  public String getCode() { return name(); }

  @JsonCreator
  public static ReportReason from(String code){
    for (ReportReason reportReason : ReportReason.values()) {
      if(reportReason.getCode().equals(code)){
        return reportReason;
      }
    }
    throw new CustomException(ErrorCode.INVALID_REPORT_REASON);
  }

  // TODO: 신고 사유 재정의 필요
}