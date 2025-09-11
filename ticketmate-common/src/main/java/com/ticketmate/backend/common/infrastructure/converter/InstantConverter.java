package com.ticketmate.backend.common.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 모든 Instant 값을 DB 저장 직전에 '초' 단위로 절삭
 */
@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Instant> {

  @Override
  public Instant convertToDatabaseColumn(Instant instant) {
    return instant == null ? null : instant.truncatedTo(ChronoUnit.SECONDS);
  }

  @Override
  public Instant convertToEntityAttribute(Instant instant) {
    return instant;
  }
}
