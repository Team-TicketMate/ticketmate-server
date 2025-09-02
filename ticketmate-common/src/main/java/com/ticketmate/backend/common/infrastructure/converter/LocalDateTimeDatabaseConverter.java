package com.ticketmate.backend.common.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 모든 LocalDateTime 값 DB 저장 직전에 초 단위 절삭
 * autoApply = true 설정으로 프로젝트 전역의 LocalDateTime column 자동 적용
 */
@Converter(autoApply = true)
public class LocalDateTimeDatabaseConverter implements AttributeConverter<LocalDateTime, LocalDateTime> {

  @Override
  public LocalDateTime convertToDatabaseColumn(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.truncatedTo(ChronoUnit.SECONDS);
  }

  @Override
  public LocalDateTime convertToEntityAttribute(LocalDateTime localDateTime) {
    return localDateTime;
  }
}
