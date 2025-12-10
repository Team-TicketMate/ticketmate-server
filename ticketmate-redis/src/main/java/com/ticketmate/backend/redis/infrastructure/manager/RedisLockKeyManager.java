package com.ticketmate.backend.redis.infrastructure.manager;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.redis.core.constant.RedisLockKeyDomain;
import com.ticketmate.backend.redis.infrastructure.util.RedisLockKeyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("redisLockKeyManager")
@Slf4j
public class RedisLockKeyManager {

  public String generate(String domain, Object... segments) {
    RedisLockKeyDomain redisLockKeyDomain = validateDomain(domain);

    RedisLockKeyBuilder builder = RedisLockKeyBuilder.of(redisLockKeyDomain.getDomainKey());
    if (segments != null) {
      for (Object segment : segments) {
        if (segment == null) {
          log.error("Redis Lock Key Segment 조합 중 비어있는 값이 있습니다.");
          throw new CustomException(ErrorCode.INVALID_LOCK_KEY_SEGMENT);
        }
        builder.append(segment);
      }
    }
    return builder.build();
  }

  private RedisLockKeyDomain validateDomain(String domain) {
    if (CommonUtil.nvl(domain, "").isEmpty()) {
      log.error("Redis Lock Key 생성을 위한 Domain이 비어있습니다.");
      throw new CustomException(ErrorCode.INVALID_LOCK_KEY_DOMAIN);
    }
    return RedisLockKeyDomain.fromDomainKey(domain);
  }
}
