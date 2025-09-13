package com.ticketmate.backend.common.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NullIfBlankConverter implements AttributeConverter<String, String> {

  @Override
  public String convertToDatabaseColumn(String s) {
    return (s != null && s.isBlank()) ? null : s;
  }

  @Override
  public String convertToEntityAttribute(String s) {
    return "";
  }
}
