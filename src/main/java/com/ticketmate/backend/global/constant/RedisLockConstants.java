package com.ticketmate.backend.global.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class RedisLockConstants {

  // Redis Lock
  public static final Long WAIT_TIME = 5L; // Lock 획득을 위한 대기 시간
  public static final Long LEASE_TIME = 2L; // Lock 획득 후 만료 시간

}
