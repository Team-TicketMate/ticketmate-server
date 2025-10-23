package com.ticketmate.backend.common.infrastructure.converter;

import com.ticketmate.backend.common.core.util.CommonUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NullIfBlankConverter implements AttributeConverter<String, String> {

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (CommonUtil.nvl(attribute, "").isEmpty()) {
      return null;
    }
    String trimmed = attribute.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    return dbData;
  }
}
