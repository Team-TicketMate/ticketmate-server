package com.ticketmate.backend.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberPlatform {
  ANDROID("안드로이드"),
  IOS("애플"),
  WEB("웹"),
  OTHER("기타");

  private final String description;
}
