package com.ticketmate.backend.global.util.database;

import static com.ticketmate.backend.global.constant.PageableConstants.DEFAULT_PAGE_SIZE;
import static com.ticketmate.backend.global.constant.PageableConstants.DEFAULT_SORT_DIRECTION;
import static com.ticketmate.backend.global.constant.PageableConstants.DEFAULT_SORT_FIELD;
import static com.ticketmate.backend.global.constant.PageableConstants.MAX_PAGE_SIZE;

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
   * @param pageSize 사용자 입력 페이지 크기
   * @return 검증된 페이지 크기
   */
  public int validatePageSize(Integer pageSize) {
    if (pageSize == null || pageSize < 1) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(pageSize, MAX_PAGE_SIZE); // 최대값 제한
  }

  /**
   * 페이지 크기 검증 및 기본값 설정 (기본 페이지 크기 검증 메서드 오버로딩)
   *
   * @param pageSize        사용자 입력 페이지 크기
   * @param defaultPageSize 도메인별 기본 페이지 크기(ex: 채팅메시지의 경우 20)
   * @return 검증된 페이지 크기
   */
  public int validatePageSize(Integer pageSize, int defaultPageSize) {
    if (pageSize == null || pageSize < 1) {
      return defaultPageSize;
    }
    return Math.min(pageSize, MAX_PAGE_SIZE); // 최대값 제한
  }

  /**
   * 정렬 필드 검증 및 기본값 설정
   *
   * @param sortField     사용자 입력 정렬 필드
   * @param allowedFields 허용된 정렬 필드들
   * @return 검증된 정렬 필드
   */
  public String validateSortField(String sortField, String... allowedFields) {
    if (sortField == null || sortField.trim().isEmpty()) {
      return DEFAULT_SORT_FIELD;
    }

    // 허용된 필드가 지정되지 않은 경우 기본값 반환
    if (allowedFields == null) {
      return DEFAULT_SORT_FIELD;
    }

    // 허용된 필드 중 하나인지 검증
    for (String allowedField : allowedFields) {
      if (allowedField.equals(sortField)) {
        return sortField;
      }
    }

    return DEFAULT_SORT_FIELD;
  }

  /**
   * 정렬 방향 검증 및 기본값 설정
   *
   * @param sortDirection 사용자 입력 정렬 방향
   * @return 검증된 정렬 방향
   */
  public String validateSortDirection(String sortDirection) {
    if (sortDirection == null || sortDirection.trim().isEmpty()) {
      return DEFAULT_SORT_DIRECTION;
    }

    if ("ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection)) {
      return sortDirection.toUpperCase();
    }

    return DEFAULT_SORT_DIRECTION;
  }

  /**
   * Pageable 객체 생성 (사용자 입력 페이지 번호를 인덱스로 변환)
   *
   * @param pageNumber        사용자 입력 페이지 번호 (1부터 시작)
   * @param pageSize          페이지 크기
   * @param sortField         정렬 필드
   * @param sortDirection     정렬 방향
   * @param allowedSortFields 허용된 정렬 필드들
   * @return Pageable 객체
   */
  public Pageable createPageable(
      Integer pageNumber,
      Integer pageSize,
      String sortField,
      String sortDirection,
      String... allowedSortFields) {

    int pageIndex = convertToPageIndex(pageNumber);
    int validatedPageSize = validatePageSize(pageSize);
    String validatedSortField = validateSortField(sortField, allowedSortFields);
    String validatedSortDirection = validateSortDirection(sortDirection);

    Sort sort = Sort.by(Sort.Direction.fromString(validatedSortDirection), validatedSortField);

    return PageRequest.of(pageIndex, validatedPageSize, sort);
  }

  /**
   * Pageable 객체 생성 (사용자 입력 페이지 번호를 인덱스로 변환) [위 기본 메서드 오버로딩]
   *
   * @param pageNumber        사용자 입력 페이지 번호 (1부터 시작)
   * @param pageSize          페이지 크기
   * @param defaultPageSize   도메인별 페이지 사이즈
   * @param sortField         정렬 필드
   * @param sortDirection     정렬 방향
   * @param allowedSortFields 허용된 정렬 필드들
   * @return Pageable 객체
   */
  public Pageable createPageable(
      Integer pageNumber,
      Integer pageSize,
      int defaultPageSize,
      String sortField,
      String sortDirection,
      String... allowedSortFields) {

    int pageIndex = convertToPageIndex(pageNumber);
    int validatedSize = validatePageSize(pageSize, defaultPageSize);
    String validatedField = validateSortField(sortField, allowedSortFields);
    String validatedDir = validateSortDirection(sortDirection);

    Sort sort = Sort.by(Sort.Direction.fromString(validatedDir), validatedField);
    return PageRequest.of(pageIndex, validatedSize, sort);
  }

  /**
   * 기본 Pageable 객체 생성 (기본값 사용)
   *
   * @return 기본 Pageable 객체
   */
  public Pageable createDefaultPageable() {
    return createPageable(1, DEFAULT_PAGE_SIZE, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
  }
} 