package com.ticketmate.backend.member.infrastructure.constant;

import java.time.Duration;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BlockConstants {

  public static final Duration WITHDRAW_BLOCK_DURATION = Duration.ofDays(30);

  public static final int WITHDRAW_REASON_MAX_SIZE = 20;
}
