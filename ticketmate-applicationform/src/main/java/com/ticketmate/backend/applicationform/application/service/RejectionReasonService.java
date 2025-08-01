package com.ticketmate.backend.applicationform.application.service;

import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.applicationform.application.dto.response.RejectionReasonResponse;
import com.ticketmate.backend.applicationform.application.mapper.ApplicationFormMapper;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason;
import com.ticketmate.backend.applicationform.infrastructure.repository.RejectionReasonRepository;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RejectionReasonService {

  private final RejectionReasonRepository rejectionReasonRepository;
  private final ApplicationFormMapper applicationFormMapper;

  /**
   * 신청서 거절 사유(RejectionReason) 저장 or 업데이트
   *
   * @param applicationForm 거절할 신청서
   * @param request         거절 사유 & 메모
   * @return 저장된 RejectionReason 엔티티
   */
  @Transactional
  public RejectionReason saveOrUpdateRejectionReason(ApplicationForm applicationForm, ApplicationFormRejectRequest request) {
    return rejectionReasonRepository.findByApplicationFormApplicationFormId(applicationForm.getApplicationFormId())
        .map(existingReason -> updateRejectionReason(existingReason, request))
        .orElseGet(() -> createRejectionReason(applicationForm, request));
  }

  /**
   * 신청서 거절 사유(RejectionReason) 조회
   *
   * @param applicationFormId 신청서 PK
   */
  @Transactional(readOnly = true)
  public RejectionReasonResponse getRejectionReasonInfo(UUID applicationFormId) {
    return rejectionReasonRepository
        .findByApplicationFormApplicationFormId(applicationFormId)
        .map(applicationFormMapper::toRejectionReasonResponse)
        .orElseThrow(() -> new CustomException(ErrorCode.REJECTION_REASON_NOT_FOUND));
  }

  /**
   * 기존 RejectionReason 업데이트 및 저장
   *
   * @param rejectionReason DB에 저장되어있는 기존 RejectionReason 엔티티
   * @param request         새로운 거절사유 & 메모
   * @return 업데이트 된 RejectionReason 엔티티
   */
  private RejectionReason updateRejectionReason(RejectionReason rejectionReason, ApplicationFormRejectRequest request) {
    rejectionReason.setApplicationFormRejectedType(request.getApplicationFormRejectedType());
    rejectionReason.setOtherMemo(request.getOtherMemo());
    return rejectionReasonRepository.save(rejectionReason);
  }

  /**
   * 새로운 RejectionReason 생성 및 저장
   *
   * @param applicationForm 거절할 신청서
   * @param request         거절사유 & 메모
   * @return 저장된 RejectionReason 엔티티
   */
  private RejectionReason createRejectionReason(ApplicationForm applicationForm, ApplicationFormRejectRequest request) {
    RejectionReason rejectionReason = RejectionReason.builder()
        .applicationForm(applicationForm)
        .applicationFormRejectedType(request.getApplicationFormRejectedType())
        .otherMemo(request.getOtherMemo())
        .build();
    return rejectionReasonRepository.save(rejectionReason);
  }

}
