package com.ticketmate.backend.common.infrastructure.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtil {

  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

  /**
   * Instant -> LocalDateTime
   */
  public LocalDateTime toLocalDateTime(Instant instant) {
    if (instant == null) {
      return null;
    }
    return LocalDateTime.ofInstant(instant, ZONE_ID);
  }

  /**
   * LocalDateTime -> Instant
   */
  public Instant toInstant(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atZone(ZONE_ID).toInstant();
  }

  /**
   * 현재 시간을 Instant로 반환 (초 단위 절삭)
   */
  public Instant now() {
    return Instant.now().truncatedTo(ChronoUnit.SECONDS);
  }
}
