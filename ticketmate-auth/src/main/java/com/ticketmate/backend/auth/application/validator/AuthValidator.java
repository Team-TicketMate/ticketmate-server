package com.ticketmate.backend.auth.application.validator;

import static com.ticketmate.backend.member.core.constant.AccountStatus.ACTIVE;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.core.constant.AccountStatus;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthValidator {

  /**
   * 로그인 가능 여부 boolean
   */
  public boolean isLoginAllowed(Member member) {
    return member.getAccountStatus() == ACTIVE;
  }

  /**
   * 로그인 가능 여부 검증 exception
   */
  public void assertLoginAllowed(Member member) {
    if (!isLoginAllowed(member)) {
      log.warn("로그인 불가 상태의 계정입니다. accountStatus: {}, memberId: {}", member.getAccountStatus(), member.getMemberId());
      ErrorCode errorCode = resolveLoginRestrictionErrorCode(member.getAccountStatus());
      throw new CustomException(errorCode);
    }
  }

  /**
   * 비활성 계정 ErrorCode 반환
   */
  private ErrorCode resolveLoginRestrictionErrorCode(AccountStatus accountStatus) {
    return switch (accountStatus) {
      case WITHDRAWN -> ErrorCode.ACCOUNT_WITHDRAWN;
      case TEMP_BAN -> ErrorCode.ACCOUNT_TEMP_BANNED;
      case PERMANENT_BAN -> ErrorCode.ACCOUNT_PERMANENT_BANNED;
      default -> throw new CustomException(ErrorCode.INVALID_ACCOUNT_STATUS);
    };
  }
}
