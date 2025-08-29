package com.ticketmate.backend.applicationform.application.validator;

import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.HOPE_AREA_MAX_SIZE;

import com.ticketmate.backend.applicationform.application.dto.request.HopeAreaRequest;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HopeAreaValidator {

  private final List<HopeAreaRequest> requestList;

  private HopeAreaValidator(List<HopeAreaRequest> requestList) {
    this.requestList = requestList;
  }

  public HopeAreaValidator of(List<HopeAreaRequest> requestList) {
    return new HopeAreaValidator(requestList);
  }

  /**
   * 희망구역 최대 개수 검증
   */
  public HopeAreaValidator maxSize(int maxSize) {
    if (!CommonUtil.nullOrEmpty(requestList) && requestList.size() > maxSize) {
      log.error("희망구역 개수 초과: {}개 요청 (허용: {}개)", requestList.size(), maxSize);
      throw new CustomException(ErrorCode.HOPE_AREAS_SIZE_EXCEED, HOPE_AREA_MAX_SIZE);
    }
    return this;
  }

  /**
   * 희망구역 우선순위 검증
   */
  public HopeAreaValidator priorityUnique() {
    if (!CommonUtil.nullOrEmpty(requestList)) {
      return this;
    }
    Set<Integer> prioritySet = new HashSet<>();
    for (HopeAreaRequest request : requestList) {
      if (!prioritySet.add(request.getPriority())) {
        log.error("희망구역 priority 중복: {}", request.getPriority());
        throw new CustomException(ErrorCode.PRIORITY_ALREADY_EXISTS);
      }
    }
    return this;
  }

}
