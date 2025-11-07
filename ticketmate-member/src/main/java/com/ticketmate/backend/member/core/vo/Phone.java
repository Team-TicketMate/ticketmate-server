package com.ticketmate.backend.member.core.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Phone {

  private static final Pattern E164_KR_PATTERN = Pattern.compile("^\\+8210\\d{8}$");
  private static final Pattern DOMESTIC_010_PATTERN = Pattern.compile("^010\\d{8}$");

  private static final String KR_PHONE_PREFIX = "+82";

  private final String value; // +821012345678

  private Phone(String value) {
    validate(value);
    this.value = value;
  }

  /**
   * raw 전화번호를 입력받아 Phone 객체 생성
   * 허용 입력:
   * - 국내: 010-1234-5678 / 010 1234 5678 / 01012345678
   * - 국제: +821012345678 / +82 10 1234 5678
   */
  @JsonCreator(mode = Mode.DELEGATING)
  public static Phone of(String raw) {
    if (CommonUtil.nvl(raw, "").isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_PHONE);
    }
    String trimmed = raw.trim();
    if (trimmed.startsWith("+")) {
      return fromE164(trimmed);
    }
    return fromDomestic(trimmed);
  }

  /**
   * E.164 형식 -> 01012345678 형식 변환
   * Jackson @JsonValue 에 의해 응답 시 자동 적용
   */
  @JsonValue
  public String asDomesticDigits() {
    if (!E164_KR_PATTERN.matcher(this.value).matches()) {
      throw new CustomException(ErrorCode.INVALID_PHONE);
    }
    String digits = value.substring(KR_PHONE_PREFIX.length()); // 1012345678
    return "0" + digits; // 01012345678
  }

  // 국내 전화번호 형식 -> E.164 형식
  private static Phone fromDomestic(String input) {
    String normalized = input.replaceAll("[^0-9]", "");
    if (!DOMESTIC_010_PATTERN.matcher(normalized).matches()) {
      throw new CustomException(ErrorCode.INVALID_PHONE);
    }
    String e164 = KR_PHONE_PREFIX + normalized.substring(1); // +821012345678
    return new Phone(e164);
  }

  // E.164 형식 입력
  private static Phone fromE164(String input) {
    String normalized = input.replaceAll("[^0-9+]", "");
    return new Phone(normalized);
  }

  private void validate(String value) {
    Optional.ofNullable(value)
      .filter(v -> !v.isBlank())
      .filter(this::isValidFormat)
      .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PHONE));
  }

  private boolean isValidFormat(String value) {
    return E164_KR_PATTERN.matcher(value).matches();
  }
}
