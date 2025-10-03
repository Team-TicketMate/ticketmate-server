package com.ticketmate.backend.crypto.infrastructure.converter;

import com.ticketmate.backend.crypto.infrastructure.provider.AesGcmProvider;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;

@Converter(autoApply = false)
@AllArgsConstructor
public class AesGcmConverter implements AttributeConverter<String, String> {
  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null) return null;
    return AesGcmProvider.encrypt(attribute);
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return AesGcmProvider.decrypt(dbData);
  }
}
