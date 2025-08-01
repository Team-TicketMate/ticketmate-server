package com.ticketmate.backend.common.core.constant;

/**
 * 각 도메인이 제공하는 정렬 키 enum 이 구현해야 할 인터페이스
 */
public interface SortField {

  // 실제 Sort.by() 에 넘길 프로퍼티 이름 (엔티티 속성명 ex.createdDate)
  String getProperty();
}
