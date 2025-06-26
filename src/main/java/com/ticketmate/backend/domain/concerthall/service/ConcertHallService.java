package com.ticketmate.backend.domain.concerthall.service;

import static com.ticketmate.backend.global.util.common.CommonUtil.enumToString;
import static com.ticketmate.backend.global.util.common.CommonUtil.nvl;

import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import com.ticketmate.backend.domain.concerthall.domain.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallInfoResponse;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.concerthall.repository.ConcertHallRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.PageableUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertHallService {

  private final ConcertHallRepository concertHallRepository;
  private final EntityMapper entityMapper;

  /**
   * 공연장 정보 필터링 로직
   *
   * 필터링 조건: 공연장 이름 (검색어), 도시
   * 정렬 조건: created_date
   *
   * @param request concertHallName 공연장 이름 검색어 (빈 문자열인 경우 필터링 제외)
   *                cityCode 지역 코드 (null 인 경우 필터링 제외)
   *                pageNumber 요청 페이지 번호 (1부터 시작, 기본 1)
   *                pageSize 한 페이지 당 항목 수 (기본 10)
   *                sortField 정렬할 필드 (기본: created_date)
   *                sortDirection 정렬 방향 (기본: DESC)
   */
  @Transactional(readOnly = true)
  public Page<ConcertHallFilteredResponse> filteredConcertHall(ConcertHallFilteredRequest request) {

    Page<ConcertHall> concertHallPage = getConcertHallPage(request);

    // 엔티티를 DTO로 변환하여 Page 객체로 매핑
    return concertHallPage.map(entityMapper::toConcertHallFilteredResponse);
  }

  public Page<ConcertHall> getConcertHallPage(ConcertHallFilteredRequest request) {

    // String, Integer 값 검증
    String concertHallName = nvl(request.getConcertHallName(), "");
    String city = "";

    // 지역 코드에 해당하는 City 반환
    if (request.getCityCode() != null) {
      city = enumToString(City.fromCityCode(request.getCityCode()));
    }

    // Pageable 객체 생성 (PageableUtil 사용)
    Pageable pageable = PageableUtil.createPageable(
        request.getPageNumber(),
        request.getPageSize(),
        request.getSortField(),
        request.getSortDirection(),
        "created_date"
    );

    return concertHallRepository
        .filteredConcertHall(concertHallName, city, pageable);
  }

  /**
   * 공연장 정보 상세조회
   */
  @Transactional(readOnly = true)
  public ConcertHallInfoResponse getConcertHallInfo(UUID concertHallId) {

    ConcertHall concertHall = concertHallRepository.findById(concertHallId)
        .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND));

    return entityMapper.toConcertHallInfoResponse(concertHall);
  }

  /**
   * 중복된 공연장 명 검증
   */
  public void validateDuplicateConcertHallName(String concertHallName) {
    if (concertHallRepository.existsByConcertHallName(concertHallName)) {
      log.error("중복된 공연장 이름입니다. 요청된 공연장 이름: {}", concertHallName);
      throw new CustomException(ErrorCode.DUPLICATE_CONCERT_HALL_NAME);
    }
  }
}
