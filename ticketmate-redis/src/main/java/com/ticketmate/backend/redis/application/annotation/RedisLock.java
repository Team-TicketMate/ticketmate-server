package com.ticketmate.backend.redis.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {

  /**
   * Lock Key를 위한 SpEL 표현식
   * ex) "'member:' + #memberId"
   */
  String key();

  /**
   * 락 획득을 위해 기다리는 최대 시간 (기본: 5초)
   */
  long waitTime() default 5L;

  /**
   * 락 자동해제 시간 (기본: 5초)
   */
  long leaseTime() default 5L;

  /**
   * 시간 단위 (기본: 초)
   */
  TimeUnit timeUnit() default TimeUnit.SECONDS;
}
