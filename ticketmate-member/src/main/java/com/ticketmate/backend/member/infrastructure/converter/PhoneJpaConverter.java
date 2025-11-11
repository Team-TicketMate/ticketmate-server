package com.ticketmate.backend.member.infrastructure.converter;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.core.vo.Phone;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PhoneJpaConverter implements AttributeConverter<Phone, String> {

  @Override
  public String convertToDatabaseColumn(Phone phone) {
    if (phone == null) {
      return null;
    }
    return phone.getValue();
  }

  @Override
  public Phone convertToEntityAttribute(String string) {
    if (CommonUtil.nvl(string, "").isEmpty()) {
      return null;
    }
    return Phone.of(string);
  }
}
