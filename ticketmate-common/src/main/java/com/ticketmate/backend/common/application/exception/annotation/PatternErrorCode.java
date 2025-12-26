package com.ticketmate.backend.common.application.exception.annotation;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Pattern validation 실패 시 사용할 ErrorCode를 지정합니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PatternErrorCode {

  ErrorCode value();
}
