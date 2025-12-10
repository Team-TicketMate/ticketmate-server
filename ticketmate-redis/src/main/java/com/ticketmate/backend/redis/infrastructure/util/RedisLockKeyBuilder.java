package com.ticketmate.backend.redis.infrastructure.util;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;

public final class RedisLockKeyBuilder {

  private static final String LOCK_KEY_PREFIX = "lock:";

  private static final String DELIMITER = ":";

  private final StringBuilder builder;

  private RedisLockKeyBuilder(String domain) {
    if (CommonUtil.nvl(domain, "").isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_LOCK_KEY_DOMAIN);
    }
    this.builder = new StringBuilder()
      .append(LOCK_KEY_PREFIX)
      .append(domain);
  }

  /**
   * Lock Key 생성을 위한 빌더 객체 생성
   */
  public static RedisLockKeyBuilder of(String domain) {
    return new RedisLockKeyBuilder(domain);
  }

  public RedisLockKeyBuilder append(Object value) {
    if (value == null || value.toString().isBlank()) {
      throw new CustomException(ErrorCode.INVALID_LOCK_KEY_SEGMENT);
    }
    this.builder
      .append(DELIMITER)
      .append(value);
    return this;
  }

  /**
   * 최종 Lock Key 문자열 반환
   */
  public String build() {
    return this.builder.toString();
  }
}
