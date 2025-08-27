package com.ticketmate.backend.admin.concerthall.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;

import com.ticketmate.backend.admin.concerthall.application.dto.request.ConcertHallInfoEditRequest;
import com.ticketmate.backend.admin.concerthall.application.dto.request.ConcertHallInfoRequest;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.concerthall.application.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.concerthall.application.service.ConcertHallService;
import com.ticketmate.backend.concerthall.core.constant.City;
import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import com.ticketmate.backend.concerthall.infrastructure.repository.ConcertHallRepository;
import com.ticketmate.backend.concerthall.infrastructure.repository.ConcertHallRepositoryCustom;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertHallAdminService {

  private final ConcertHallService concertHallService;
  private final ConcertHallRepository concertHallRepository;
  private final ConcertHallRepositoryCustom concertHallRepositoryCustom;

  /**
   * 공연장 정보 저장
   * 관리자만 저장 가능합니다
   *
   * @param request concertHallName 공연장 명
   *                address 주소
   *                webSiteUrl 웹사이트 URL
   */
  @Transactional
  public void saveConcertHallInfo(ConcertHallInfoRequest request) {

    // 중복된 공연장이름 검증
    concertHallService.validateDuplicateConcertHallName(request.getConcertHallName());

    concertHallRepository.save(ConcertHall.builder()
        .concertHallName(request.getConcertHallName())
        .address(request.getAddress()) // 요청된 주소에 따른 지역코드 할당 (없는경우 null)
        .city(City.fromAddress(request.getAddress()))
        .webSiteUrl(request.getWebSiteUrl())
        .build());
  }

  /**
   * 공연장 정보 필터링 로직
   * 필터링 조건: 공연장 이름 (검색어), 도시
   * 정렬 조건: createdDate
   *
   * @param request concertHallName 공연장 이름 검색어 (빈 문자열인 경우 필터링 제외)
   *                cityCode 지역 코드 (null 인 경우 필터링 제외)
   *                pageNumber 요청 페이지 번호 (기본 1)
   *                pageSize 한 페이지 당 항목 수 (기본 10)
   *                sortField 정렬할 필드 (기본: createdDate)
   *                sortDirection 정렬 방향 (기본: DESC)
   */
  @Transactional(readOnly = true)
  public Page<ConcertHallFilteredResponse> filteredConcertHall(ConcertHallFilteredRequest request) {

    City city = request.getCityCode() != null
        ? City.fromCityCode(request.getCityCode())
        : null;

    return concertHallRepositoryCustom.filteredConcertHall(
        request.getConcertHallName(),
        city,
        request.toPageable()
    );
  }

  /**
   * 공연장 정보 수정
   *
   * @param request concertHallId 공연장 PK
   *                concertHallName 공연장 명
   *                address 주소
   *                webSiteUrl 사이트 URL
   */
  @Transactional
  public void editConcertHallInfo(UUID concertHallId, ConcertHallInfoEditRequest request) {
    Optional.of(concertHallId)
        .map(concertHallService::findConcertHallById)
        .map(concertHall -> editConcertHallEntity(concertHall, request))
        .map(concertHallRepository::save)
        .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_SAVE_ERROR));
  }

  /**
   * 사용자가 요청한 정보에 따라 공연장 정보를 수정합니다
   */
  private ConcertHall editConcertHallEntity(ConcertHall concertHall, ConcertHallInfoEditRequest request) {
    // 정보 업데이트
    if (!nvl(request.getConcertHallName(), "").isEmpty()) {
      log.debug("새로운 공연장 명: {}", request.getConcertHallName());
      concertHall.setConcertHallName(request.getConcertHallName());
    }
    if (!nvl(request.getAddress(), "").isEmpty()) {
      log.debug("새로운 주소: {}", request.getAddress());
      concertHall.setAddress(request.getAddress()); // 주소 업데이트

      City city = City.fromAddress(request.getAddress());
      concertHall.setCity(city); // 지역 코드 업데이트
    }
    if (!nvl(request.getWebSiteUrl(), "").isEmpty()) {
      log.debug("새로운 웹사이트 URL: {}", request.getWebSiteUrl());
      concertHall.setWebSiteUrl(request.getWebSiteUrl());
    }
    return concertHall;
  }
}
