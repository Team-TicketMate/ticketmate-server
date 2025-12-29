package com.ticketmate.backend.applicationform.application.validator;


import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormDetailRequest;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationFormDetailValidator {

  private final List<ApplicationFormDetailRequest> requestList;

  private ApplicationFormDetailValidator(List<ApplicationFormDetailRequest> requestList) {
    if (CommonUtil.nullOrEmpty(requestList)) {
      log.error("신청서에는 최소 1개 이상의 신청서 세부사항이 포함되어야 합니다. 요청 리스트가 비었습니다.");
      throw new CustomException(ErrorCode.APPLICATION_FORM_DETAIL_REQUIRED);
    }
    this.requestList = requestList;
  }

  public static ApplicationFormDetailValidator of(List<ApplicationFormDetailRequest> requestList) {
    return new ApplicationFormDetailValidator(requestList);
  }

  /**
   * 신청서 세부사항 공연일자 검증 (null 금지 + 중복 금지)
   */
  public ApplicationFormDetailValidator performanceDateNonNullAndDistinct() {
    Set<LocalDateTime> performanceDateSet = new HashSet<>();
    for (ApplicationFormDetailRequest request : requestList) {
      if (request.getPerformanceDate() == null) {
        log.error("요청된 신청서 세부사항의 공연일자가 null 입니다.");
        throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
      }
      if (!performanceDateSet.add(request.getPerformanceDate())) {
        log.error("중복된 공연일자가 존재합니다: {}", request.getPerformanceDate());
        throw new CustomException(ErrorCode.DUPLICATE_CONCERT_DATE);
      }
    }
    return this;
  }

  /**
   * 요청 매수 검증 (최소 ~ 최대)
   */
  public ApplicationFormDetailValidator requestCountRange(int minInclusive, int maxInclusive) {
    for (ApplicationFormDetailRequest request : requestList) {
      int requestCount = request.getRequestCount();
      if (requestCount < minInclusive || requestCount > maxInclusive) {
        log.error("대리 신청서 매수는 최소 {}장, 최대 {}장까지 가능합니다. 요청된 예매 매수: {}", minInclusive, maxInclusive, requestCount);
        throw new CustomException(ErrorCode.TICKET_REQUEST_COUNT_EXCEED);
      }
    }
    return this;
  }

  /**
   * 요청사항(requirement) 최대 길이 검증
   */
  public ApplicationFormDetailValidator requirementMaxLength(int maxLengthInclusive) {
    for (ApplicationFormDetailRequest request : requestList) {
      String requirement = request.getRequirement();
      if (requirement != null && requirement.length() > maxLengthInclusive) {
        log.error("요청사항 최대 길이 초과: {}자 (허용: {}자)", requirement.length(), maxLengthInclusive);
        throw new CustomException(ErrorCode.APPLICATION_FORM_REQUIREMENT_LENGTH_EXCEED);
      }
    }
    return this;
  }

  /**
   * 희망구역(HopeArea) 리스트 검증
   */
  public ApplicationFormDetailValidator hopeAreaList(int maxSize) {
    for (ApplicationFormDetailRequest request : requestList) {
      HopeAreaValidator
        .of(request.getHopeAreaList())
        .maxSize(maxSize)
        .priorityUnique();
    }
    return this;
  }
}
