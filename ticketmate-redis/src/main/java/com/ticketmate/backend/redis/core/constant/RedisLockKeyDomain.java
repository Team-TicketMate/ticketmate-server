package com.ticketmate.backend.redis.core.constant;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisLockKeyDomain {
  MEMBER("member"),
  CONCERT("concert"),
  CONCERT_HALL("concert-hall"),
  PORTFOLIO("portfolio"),
  APPLICATION_FORM("application-form"),
  REPORT("report");

  private final String domainKey;

  public static RedisLockKeyDomain fromDomainKey(String domainKey) {
    for (RedisLockKeyDomain value : RedisLockKeyDomain.values()) {
      if (value.domainKey.equals(domainKey)) {
        return value;
      }
    }
    throw new CustomException(ErrorCode.REDIS_LOCK_DOMAIN_NOT_FOUND);
  }
}
