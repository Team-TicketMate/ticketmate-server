package com.ticketmate.backend.auth.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
    /** 여러 관리자 계정 정보 리스트 */
    @NotEmpty List<Admin> admins
) {

  /**
   * 개별 관리자 계정 정보
   */
  public record Admin(
      @NotBlank String username,
      @NotBlank String password,
      @NotBlank String name
  ) {

  }
}
