package com.ticketmate.backend.concert.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConcertType {
  CONCERT("콘서트"),

  MUSICAL("뮤지컬"),

  SPORTS("스포츠"),

  CLASSIC("클래식"),

  EXHIBITIONS("전시"),

  OPERA("오페라"),

  ETC("기타");


  private final String description;
}
