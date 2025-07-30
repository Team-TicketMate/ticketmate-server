package com.ticketmate.backend.common.infrastructure.util;

import com.ticketmate.backend.common.core.constant.SortField;
import com.ticketmate.backend.common.infrastructure.constant.PageableConstants;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 페이지네이션을 중앙에서 관리하는 유틸리티 클래스
 * 사용자는 1부터 시작하는 페이지 번호를 입력하고, 내부적으로 인덱스 기반으로 변환
 */
@UtilityClass
public class PageableUtil {

  /**
   * 사용자 입력 페이지 번호(1부터 시작)를 인덱스 기반으로 변환
   *
   * @param pageNumber 사용자 입력 페이지 번호 (1부터 시작)
   * @return 인덱스 기반 페이지 번호 (0부터 시작)
   */
  public int convertToPageIndex(Integer pageNumber) {
    if (pageNumber == null || pageNumber < 1) {
      return 0; // 기본값은 첫 번째 페이지 (인덱스 0)
    }
    return pageNumber - 1; // 1부터 시작하는 페이지 번호를 0부터 시작하는 인덱스로 변환
  }

  /**
   * 페이지 크기 검증 및 기본값 설정
   *
   * @param pageSize        사용자 입력 페이지 크기
   * @param defaultPageSize 도메인별 기본 페이지 크기(ex: 채팅메시지의 경우 20)
   * @return 검증된 페이지 크기
   */
  public int validatePageSize(Integer pageSize, Integer defaultPageSize) {
    if (pageSize == null || pageSize < 1) {
      return defaultPageSize;
    }
    return Math.min(pageSize, PageableConstants.MAX_PAGE_SIZE); // 최대값 제한
  }

  /**
   * Pageable 객체 생성 (사용자 입력 페이지 번호를 인덱스로 변환)
   *
   * @param <SF>              enum 타입, SortField를 구현해야함
   * @param pageNumber        사용자 입력 페이지 번호 (1부터 시작)
   * @param pageSize          페이지 크기
   * @param defaultPageSize   도메인별 기본 페이지 사이즈
   * @param sortField         정렬 필드
   * @param sortDirection     정렬 방향
   * @return Pageable 객체
   */
  public <SF extends SortField> Pageable createPageable(
      Integer pageNumber,
      Integer pageSize,
      Integer defaultPageSize,
      SF sortField,
      Sort.Direction sortDirection) {

    int pageIndex = convertToPageIndex(pageNumber);
    int validatedSize = validatePageSize(pageSize, defaultPageSize);

    if (sortField == null || sortDirection == null) {
      return PageRequest.of(pageIndex, validatedSize, Sort.unsorted());
    }

    Sort sort = Sort.by(sortDirection, sortField.getProperty());
    return PageRequest.of(pageIndex, validatedSize, sort);
  }
} 