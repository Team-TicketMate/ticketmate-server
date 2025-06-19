package com.ticketmate.backend.global.constant;

public final class RedisLockConstants {

  // Redis Lock
  public static final Long WAIT_TIME = 5L; // Lock 획득을 위한 대기 시간
  public static final Long LEASE_TIME = 2L; // Lock 획득 후 만료 시간
  private RedisLockConstants() {
    throw new UnsupportedOperationException("이 유틸리티 클래스는 인스턴스화할 수 없습니다.");
  }

}
