package com.ticketmate.backend.member.application.service;

import static com.ticketmate.backend.member.infrastructure.constant.BlockConstants.WITHDRAW_REASON_MAX_SIZE;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.application.dto.request.MemberWithdrawRequest;
import com.ticketmate.backend.member.core.constant.WithdrawalReasonType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.entity.MemberWithdrawalHistory;
import com.ticketmate.backend.member.infrastructure.repository.MemberWithdrawalHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberWithdrawalHistoryService {

  private final MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;

  @Transactional
  public void saveWithdrawalHistory(Member member, MemberWithdrawRequest request) {
    String otherReason = handleOtherReason(request.getWithdrawalReasonType(), request.getOtherReason());
    MemberWithdrawalHistory history = MemberWithdrawalHistory.create(
        member.getMemberId(),
        member.getPhone(),
        member.getNickname(),
        request.getWithdrawalReasonType(),
        otherReason
    );
    MemberWithdrawalHistory savedHistory = memberWithdrawalHistoryRepository.save(history);
    log.debug("회원: {}에 대한 회원 탈퇴 이력 저장: {}", member.getMemberId(), savedHistory.getMemberWithdrawalHistoryId());
  }

  private String handleOtherReason(WithdrawalReasonType withdrawalReasonType, String otherReason) {
    if (withdrawalReasonType == null) {
      log.error("회원 탈퇴 사유 타입이 누락되었습니다.");
      throw new CustomException(ErrorCode.WITHDRAWAL_REASON_TYPE_REQUIRED);
    }
    if (withdrawalReasonType == WithdrawalReasonType.OTHER) {
      String normalize = CommonUtil.normalizeAndRemoveSpecialCharacters(otherReason);
      if (normalize.length() > WITHDRAW_REASON_MAX_SIZE) {
        log.error("회원 탈퇴 기타 사유 글자 수 오류. 최대: {}, 요청: {}", WITHDRAW_REASON_MAX_SIZE, normalize.length());
        throw new CustomException(ErrorCode.OTHER_REASON_LENGTH_EXCEED, WITHDRAW_REASON_MAX_SIZE);
      }
      return normalize;
    }
    return null;
  }
}
