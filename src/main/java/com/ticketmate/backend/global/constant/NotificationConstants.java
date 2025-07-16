package com.ticketmate.backend.global.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class NotificationConstants {

  // FCM Token
  public static final long FCM_TOKEN_TTL = 30 * 24 * 60L; // FCM 토큰 30일 후 파기

  // Notification
  public static final String NOTIFICATION_TTL_PREFIX = "ttl";
  public static final String NOTIFICATION_TTL = "300";
  public static final String NOTIFICATION_ICON_PATH = "resources/static/ticketmate-logo.png";

  // Notification Key
  public static final String PLACEHOLDER_NICKNAME_KEY = "nickname";
  public static final String PLACEHOLDER_REJECT_OTHER_MEMO_KEY = "otherReject";
}
