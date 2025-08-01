package com.ticketmate.backend.auth.core.principal;

import java.util.List;

/**
 * 애플리케이션 전반에서 사용할 사용자 정보 계약
 */
public interface UserPrincipal {

  /**
   * 회원 고유 PK
   */
  String getMemberId();

  /**
   * 로그인 ID (username/email)
   */
  String getUsername();

  /**
   * 사용자 권한 목록
   */
  List<String> getRoles();
}
