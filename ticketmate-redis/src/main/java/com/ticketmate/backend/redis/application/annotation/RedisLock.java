package com.ticketmate.backend.redis.application.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {
  /**
   * Lock Key를 위한 SpEL 표현식
   * ex) "'member:' + #memberId"
   */
  String key();
}
