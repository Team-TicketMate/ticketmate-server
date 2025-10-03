package com.ticketmate.backend.crypto.infrastructure.constant;

import java.security.SecureRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AesGcmConstants {
  public static final String TRANS = "AES/GCM/NoPadding";
  public static final int IV_LEN = 12;   // 96bit
  public static final int TAG_LEN = 128; // bits
  public static final SecureRandom RNG = new SecureRandom();
}
