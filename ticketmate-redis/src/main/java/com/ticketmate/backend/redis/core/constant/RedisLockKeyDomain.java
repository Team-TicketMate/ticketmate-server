package com.ticketmate.backend.redis.core.constant;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisLockKeyDomain {
  MEMBER("member"),
  MEMBER_FOLLOW("member-follow"),
  AGENT_ACCOUNT("agent-account"),
  AGENT_RANKING("agent-ranking"),
  CONCERT("concert"),
  CONCERT_HALL("concert-hall"),
  PORTFOLIO("portfolio"),
  APPLICATION_FORM("application-form"),
  CONCERT_AGENT_AVAILABILITY("concert-agent-availability"),
  FULFILLMENT_FORM("fulfillment-form"),
  REVIEW("review"),
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
