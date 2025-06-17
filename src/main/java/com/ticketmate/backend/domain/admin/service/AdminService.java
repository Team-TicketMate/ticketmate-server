package com.ticketmate.backend.domain.admin.service;

import com.ticketmate.backend.domain.admin.dto.request.*;
import com.ticketmate.backend.domain.admin.dto.response.ConcertHallFilteredAdminResponse;
import com.ticketmate.backend.domain.admin.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.domain.admin.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concert.repository.ConcertDateRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepository;
import com.ticketmate.backend.domain.concert.repository.TicketOpenDateRepository;
import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import com.ticketmate.backend.domain.concerthall.domain.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.concerthall.repository.ConcertHallRepository;
import com.ticketmate.backend.domain.concerthall.service.ConcertHallService;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayloadRequest;
import com.ticketmate.backend.domain.notification.service.FcmService;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.portfolio.repository.PortfolioRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.file.constant.UploadType;
import com.ticketmate.backend.global.file.service.FileService;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ticketmate.backend.global.util.common.CommonUtil.enumToString;
import static com.ticketmate.backend.global.util.common.CommonUtil.nvl;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

  private final ConcertHallService concertHallService;
  private final ConcertHallRepository concertHallRepository;
  private final ConcertRepository concertRepository;
  private final ConcertDateRepository concertDateRepository;
  private final TicketOpenDateRepository ticketOpenDateRepository;
  private final PortfolioRepository portfolioRepository;
  private final FileService fileService;
  private final EntityMapper entityMapper;
  private final FcmService fcmService;
  private final NotificationUtil notificationUtil;


    /*
    ======================================공연======================================
     */

  /**
   * 콘서트 정보 저장
   *
   * @param request concertName 공연 제목
   *                concertHallId 공연장 PK
   *                concertType 공연 카테고리
   *                concertThumbNail 공연 썸네일 이미지
   *                seatingChart 좌석 배치도 이미지
   *                ticketReservationSite 티켓 예매처 사이트
   *                concertDateRequests 공연 날짜 DTO List
   *                ticketOpenDateRequests 티켓 오픈일 DTO List
   */
  @Transactional
  public void saveConcertInfo(ConcertInfoRequest request) {

    // 1. 중복된 공연이름 검증
    validateConcertName(request.getConcertName());

    // 2. 공연장 검색 (요청된 공연장 PK가 null이 아닌 경우)
    ConcertHall concertHall = null;
    if (request.getConcertHallId() != null) {
      concertHall = concertHallRepository.findById(request.getConcertHallId())
          .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND));
    }

    // 3. 콘서트 썸네일 저장
    String concertThumbnailUrl = fileService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);

    // 4. 좌석 배치도 저장
    String seatingChartUrl = null;
    if (request.getSeatingChart() != null) {
      seatingChartUrl = fileService.uploadFile(request.getSeatingChart(), UploadType.CONCERT);
    }

    // 5. 공연 정보 저장
    Concert concert = Concert.builder()
        .concertName(request.getConcertName())
        .concertHall(concertHall)
        .concertType(request.getConcertType())
        .concertThumbnailUrl(concertThumbnailUrl)
        .seatingChartUrl(seatingChartUrl)
        .ticketReservationSite(request.getTicketReservationSite())
        .build();
    concertRepository.save(concert);

    // 6. 공연 날짜 검증 및 저장
    validateConcertDateList(request.getConcertDateRequestList());
    List<ConcertDate> concertDateList = request.getConcertDateRequestList().stream()
        .map(dateRequest -> ConcertDate.builder()
            .concert(concert)
            .performanceDate(dateRequest.getPerformanceDate())
            .session(dateRequest.getSession())
            .build()
        )
        .collect(Collectors.toList());
    concertDateRepository.saveAll(concertDateList);

    // 7. 티켓 오픈일 검증 및 저장
    // 티켓 오픈일 요청에 선예매/일반예매 데이터가 최소 한개 이상 존재하는지 검증
    validateTicketOpenDateList(request.getTicketOpenDateRequestList());
    List<TicketOpenDate> ticketOpenDateList = request.getTicketOpenDateRequestList().stream()
        .map(ticketOpenDateRequest -> TicketOpenDate.builder()
            .concert(concert)
            .openDate(ticketOpenDateRequest.getOpenDate())
            .requestMaxCount(ticketOpenDateRequest.getRequestMaxCount())
            .isBankTransfer(ticketOpenDateRequest.getIsBankTransfer())
            .ticketOpenType(ticketOpenDateRequest.getTicketOpenType())
            .build())
        .collect(Collectors.toList());
    ticketOpenDateRepository.saveAll(ticketOpenDateList);
    log.debug("공연 정보 저장 성공: {}", request.getConcertName());
  }

  /**
   * 공연 정보 수정
   *
   * @param concertId 공연 PK
   * @param request   concertName 공연명
   *                  concertHallId 공연장 PK
   *                  concertType 공연 카테고리
   *                  concertThumbNail 공연 썸네일
   *                  seatingChart 좌석 배치도
   *                  ticketReservationSite 예매 사이트
   *                  concertDateRequestList 공연 날짜 DTO List
   *                  ticketOpenDateRequestList 티켓 오픈일 DTO List
   */
  @Transactional
  public void editConcertInfo(UUID concertId, ConcertInfoEditRequest request) {
    // 공연 조회
    Concert concert = concertRepository.findById(concertId)
        .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

    // 공연명 업데이트
    if (!nvl(request.getConcertName(), "").isEmpty()) {
      // 공연명 중복 검증
      validateConcertName(request.getConcertName());
      log.debug("공연명 업데이트: {}", request.getConcertName());
      concert.setConcertName(request.getConcertName());
    }

    // 공연장 업데이트
    if (request.getConcertHallId() != null) {
      ConcertHall concertHall = concertHallRepository.findById(request.getConcertHallId())
          .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND));
      log.debug("공연장 업데이트: {}", concertHall.getConcertHallName());
      concert.setConcertHall(concertHall);
    }

    // 공연 카테고리 업데이트
    if (request.getConcertType() != null) {
      log.debug("공연 카테고리 업데이트: {}", request.getConcertType());
      concert.setConcertType(request.getConcertType());
    }

    // 공연 썸네일 이미지 업데이트
    if (request.getConcertThumbNail() != null && !request.getConcertThumbNail().isEmpty()) {
      String newThumbnailUrl = fileService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);
      log.debug("공연 썸네일 이미지 업데이트: {}", newThumbnailUrl);
      concert.setConcertThumbnailUrl(newThumbnailUrl);
    }

    // 좌석 배치도 업데이트
    if (request.getSeatingChart() != null && !request.getSeatingChart().isEmpty()) {
      String newSeatingChartUrl = fileService.uploadFile(request.getSeatingChart(), UploadType.CONCERT);
      log.debug("좌석 배치도 이미지 업데이트: {}", newSeatingChartUrl);
      concert.setSeatingChartUrl(newSeatingChartUrl);
    }

    // 예매 사이트 업데이트
    if (request.getTicketReservationSite() != null) {
      log.debug("공연 예매 사이트 업데이트: {}", request.getTicketReservationSite());
      concert.setTicketReservationSite(request.getTicketReservationSite());
    }

    // 공연 날짜 업데이트 (기존 데이터 삭제 후 새 데이터 추가)
    List<ConcertDateRequest> concertDateRequestList = request.getConcertDateRequestList();
    if (concertDateRequestList != null && !concertDateRequestList.isEmpty()) {
      // 기존 공연 날짜 삭제
      log.debug("기존 공연 날짜를 모두 삭제합니다");
      concertDateRepository.deleteAllByConcertConcertId(concertId);

      // 새 공연 날짜 추가
      log.debug("새로운 공연 날짜를 추가합니다.");
      List<ConcertDate> concertDateList = concertDateRequestList.stream()
          .map(concertDateRequest -> ConcertDate.builder()
              .concert(concert)
              .performanceDate(concertDateRequest.getPerformanceDate())
              .session(concertDateRequest.getSession())
              .build())
          .collect(Collectors.toList());
      concertDateRepository.saveAll(concertDateList);
    }

    // 티켓 오픈일 업데이트 (기존 데이터 삭제 후 새 데이터 추가)
    List<TicketOpenDateRequest> ticketOpenDateRequestList = request.getTicketOpenDateRequestList();
    if (ticketOpenDateRequestList != null && !ticketOpenDateRequestList.isEmpty()) {
      // 기존 티켓 오픈일 삭제
      log.debug("기존 티켓 오픈일 정보를 모두 삭제합니다.");
      ticketOpenDateRepository.deleteAllByConcertConcertId(concertId);

      // 새 티켓 오픈일 추가
      log.debug("새로운 티켓 오픈일 정보를 추가합니다.");
      validateTicketOpenDateList(ticketOpenDateRequestList);
      List<TicketOpenDate> ticketOpenDateList = ticketOpenDateRequestList.stream()
          .map(ticketOpenDateRequest -> TicketOpenDate.builder()
              .concert(concert)
              .openDate(ticketOpenDateRequest.getOpenDate())
              .requestMaxCount(ticketOpenDateRequest.getRequestMaxCount())
              .isBankTransfer(ticketOpenDateRequest.getIsBankTransfer())
              .ticketOpenType(ticketOpenDateRequest.getTicketOpenType())
              .build())
          .collect(Collectors.toList());
      ticketOpenDateRepository.saveAll(ticketOpenDateList);
    }

    // 공연 정보 저장
    concertRepository.save(concert);
    log.debug("공연 정보 수정 성공: concertId={}, concertName={}", concert.getConcertId(), concert.getConcertName());
  }

  // 중복된 공연명 검증
  private void validateConcertName(String concertName) {
    if (concertRepository.existsByConcertName(concertName)) {
      log.error("중복된 공연 제목입니다. 요청된 공연 제목: {}", concertName);
      throw new CustomException(ErrorCode.DUPLICATE_CONCERT_NAME);
    }
  }

  // 공연 날짜 List 검증
  private void validateConcertDateList(List<ConcertDateRequest> concertDateRequestList) {
    if (CommonUtil.nullOrEmpty(concertDateRequestList)) {
      log.error("공연일 데이터가 요청되지 않았습니다.");
      throw new CustomException(ErrorCode.CONCERT_DATE_REQUIRED);
    }

    // 모든 회차(session) 값을 추출하여 정렬
    List<Integer> sessions = concertDateRequestList.stream()
        .map(ConcertDateRequest::getSession)
        .sorted()
        .collect(Collectors.toList());

    // 회차가 1부터 시작하는지 검증
    if (CommonUtil.nullOrEmpty(sessions) || sessions.get(0) != 1) {
      log.error("공연 회차는 반드시 1부터 시작해야 합니다.");
      throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
    }

    // 회차가 연속적으로 증가하는지 검증 (중간에 비어있는 회차가 없는지)
    for (int i = 0; i < sessions.size() - 1; i++) {
      if (sessions.get(i + 1) - sessions.get(i) != 1) {
        log.error("공연 회차는 연속적으로 증가해야 합니다. 누락된 회차 or 중복된 회차가 존재합니다: {}회차 다음에 {}회차 입력됨",
            sessions.get(i), sessions.get(i + 1));
        throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
      }
    }

    // 날짜와 회차가 올바르게 매칭되었는지 검증
    List<ConcertDateRequest> sortedByDate = concertDateRequestList.stream()
        .sorted(Comparator.comparing(ConcertDateRequest::getPerformanceDate))
        .collect(Collectors.toList());

    for (int i = 0; i < sortedByDate.size() - 1; i++) {
      LocalDateTime prevPerformanceDate = sortedByDate.get(i).getPerformanceDate();
      LocalDateTime nextPerformanceDate = sortedByDate.get(i + 1).getPerformanceDate();
      int prevSession = sortedByDate.get(i).getSession();
      int nextSession = sortedByDate.get(i + 1).getSession();

      // 날짜는 빠른데 회차가 더 늦는 경우
      if (prevSession > nextSession) {
        log.error("공연 날짜와 회차의 순서가 일치하지 않습니다. 빠른 날짜({})의 회차({})가 늦은 날짜({})의 회차({})보다 큽니다.",
            prevPerformanceDate, prevSession, nextPerformanceDate, nextSession);
        throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
      }
    }
  }

  // 티켓 오픈일 검증
  private void validateTicketOpenDateList(List<TicketOpenDateRequest> ticketOpenDateRequestList) {

    // 티켓 오픈일 null or Empty 검증
    if (CommonUtil.nullOrEmpty(ticketOpenDateRequestList)) {
      log.error("선예매/일반예매 오픈일 데이터가 요청되지 않았습니다. 최소 하나의 데이터는 필수로 입력해야합니다.");
      throw new CustomException(ErrorCode.TICKET_OPEN_DATE_REQUIRED);
    }

    // 일반 예매, 선 예매는 각각 최대 한개씩 존재 가능
    long preOpenRequestCount = ticketOpenDateRequestList.stream()
        .filter(ticketOpenDateRequest ->
            ticketOpenDateRequest.getTicketOpenType().equals(TicketOpenType.PRE_OPEN))
        .count();
    if (preOpenRequestCount > 1) {
      log.error("선예매 오픈일 데이터가 여러 개 요청되었습니다. 요청된 선예매 데이터 개수: {}개", preOpenRequestCount);
      throw new CustomException(ErrorCode.PRE_OPEN_COUNT_EXCEED);
    }

    long generalOpenRequestCount = ticketOpenDateRequestList.stream()
        .filter(ticketOpenDateRequest ->
            ticketOpenDateRequest.getTicketOpenType().equals(TicketOpenType.GENERAL_OPEN))
        .count();
    if (generalOpenRequestCount > 1) {
      log.error("일반 예매 오픈일 데이터가 여러 개 요청되었습니다. 요청된 일반예매 데이터 개수: {}개", generalOpenRequestCount);
      throw new CustomException(ErrorCode.GENERAL_OPEN_COUNT_EXCEED);
    }

    // 최대 요청 매수 검증
    ticketOpenDateRequestList.forEach(ticketOpenDateRequest -> {
      if (ticketOpenDateRequest.getRequestMaxCount() <= 0) {
        log.error("티켓팅 최대 예매 매수는 1장 이상 입력되어야합니다.");
        throw new CustomException(ErrorCode.INVALID_TICKET_REQUEST_MAX_COUNT);
      }
    });

    // 일반 예매 필수 검증 - 2025.05.16. 삭제
  }

    /*
    ======================================공연장======================================
     */

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
    if (concertHallRepository.existsByConcertHallName(request.getConcertHallName())) {
      log.error("중복된 공연장 이름입니다. 요청된 공연장 이름: {}", request.getConcertHallName());
      throw new CustomException(ErrorCode.DUPLICATE_CONCERT_HALL_NAME);
    }

    City city = null;
    // 요청된 주소에 맞는 지역코드 할당
    if (!nvl(request.getAddress(), "").isEmpty()) {
      city = City.fromAddress(request.getAddress());
    }

    log.debug("공연장 정보 저장: {}", request.getConcertHallName());
    concertHallRepository.save(ConcertHall.builder()
        .concertHallName(request.getConcertHallName())
        .address(request.getAddress())
        .city(city)
        .webSiteUrl(request.getWebSiteUrl())
        .build());
  }

  /**
   * 공연장 정보 필터링 로직
   * <p>
   * 필터링 조건: 공연장 이름 (검색어), 도시
   * 정렬 조건: created_date
   *
   * @param request concertHallName 공연장 이름 검색어 (빈 문자열인 경우 필터링 제외)
   *                cityCode 지역 코드 (null 인 경우 필터링 제외)
   *                pageNumber 요청 페이지 번호 (기본 0)
   *                pageSize 한 페이지 당 항목 수 (기본 30)
   *                sortField 정렬할 필드 (기본: created_date)
   *                sortDirection 정렬 방향 (기본: DESC)
   */
  @Transactional(readOnly = true)
  public Page<ConcertHallFilteredAdminResponse> filteredConcertHall(ConcertHallFilteredRequest request) {

    Page<ConcertHall> concertHallPage = concertHallService.getConcertHallPage(request);

    // 엔티티를 DTO로 변환하여 Page 객체로 매핑
    return concertHallPage.map(entityMapper::toConcertHallFilteredAdminResponse);
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
    ConcertHall concertHall = concertHallRepository.findById(concertHallId)
        .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND));

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

    concertHallRepository.save(concertHall);
  }

    /*
    ======================================포트폴리오======================================
     */

  /**
   * 페이지당 N개씩(기본30개) 반환합니다
   * 기본 정렬기준: 최신순
   */
  @Transactional(readOnly = true)
  public Page<PortfolioFilteredAdminResponse> filteredPortfolio(PortfolioFilteredRequest request) {

    // 요청 값 확인
    String username = nvl(request.getUsername(), "");
    String nickname = nvl(request.getNickname(), "");
    String name = nvl(request.getName(), "");
    String portfolioType = enumToString(request.getPortfolioType());

    // 정렬 조건
    Sort sort = Sort.by(
        Sort.Direction.fromString(request.getSortDirection()),
        request.getSortField()
    );

    // Pageable 생성
    Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(), sort);

    // 데이터베이스 조회
    Page<Portfolio> portfolioPage = portfolioRepository.filteredPortfolio(
        username,
        nickname,
        name,
        portfolioType,
        pageable
    );

    return portfolioPage.map(entityMapper::toPortfolioFilteredAdminResponse);
  }

  /**
   * 포트폴리오 상세조회 로직
   * 포트폴리오 PK를 입력받음
   * 해당 포트폴리오 상태를 IN_REVIEW(검토중) 으로 변경
   * 포트폴리오 대상 사용자 알림 발송
   *
   * @param portfolioId (UUID)
   */
  @Transactional
  public PortfolioForAdminResponse getPortfolio(UUID portfolioId) {
    Portfolio portfolio = portfolioRepository.findById(portfolioId)
        .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

    // 포트폴리오 작성 의뢰인 PK
    UUID clientId = portfolio.getMember().getMemberId();

    // 포트폴리오가 "검토 대기" 상태인 경우
    if (portfolio.getPortfolioType().equals(PortfolioType.PENDING_REVIEW)) {
      // 포트폴리오 상태 "검토중" (IN_REVIEW)으로 변경
      portfolio.setPortfolioType(PortfolioType.IN_REVIEW);

      // FCM 토큰이 있는 회원에게만 알림을 발송합니다.
      if (notificationUtil.existsFcmToken(clientId)) {
        // 알림 payload 설정
        NotificationPayloadRequest payload = notificationUtil.portfolioNotification(PortfolioType.IN_REVIEW, portfolio);

        // 알림 발송
        fcmService.sendNotification(clientId, payload);
      }
    }

    return entityMapper.toPortfolioForAdminResponse(portfolio);
  }

  /**
   * 관리자의 포트폴리오 승인 및 반려처리 로직
   *
   * @param request portfolioId (UUID)
   *                PortfolioType (포트폴리오 상태)
   */
  @Transactional
  public UUID reviewPortfolioCompleted(UUID portfolioId, PortfolioStatusUpdateRequest request) {
    Portfolio portfolio = portfolioRepository.findById(portfolioId)
        .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

    if (!portfolio.getPortfolioType().equals(PortfolioType.IN_REVIEW)) {
      log.error("검토중인 상태의 포트폴리오만 승인 및 반려처리 가능합니다. 요청된 포트폴리오 상태: {}", portfolio.getPortfolioType());
      throw new CustomException(ErrorCode.INVALID_PORTFOLIO_TYPE);
    }

    UUID memberId = portfolio.getMember().getMemberId();

    // 승인
    if (request.getPortfolioType().equals(PortfolioType.ACCEPTED)) {
      portfolio.setPortfolioType(PortfolioType.ACCEPTED);
      log.debug("포트폴리오: {} 승인완료: {}", portfolio.getPortfolioId(), portfolio.getPortfolioType());

      portfolio.getMember().setMemberType(MemberType.AGENT);

      if (notificationUtil.existsFcmToken(memberId)) {
        NotificationPayloadRequest payload = notificationUtil
                .portfolioNotification(PortfolioType.ACCEPTED, portfolio);

        fcmService.sendNotification(memberId, payload);
      }
    } else if (request.getPortfolioType().equals(PortfolioType.REJECTED)) {
      // 반려
      portfolio.setPortfolioType(PortfolioType.REJECTED);
      log.debug("포트폴리오: {} 반려완료: {}", portfolio.getPortfolioId(), portfolio.getPortfolioType());

      if (notificationUtil.existsFcmToken(memberId)) {

        NotificationPayloadRequest payload = notificationUtil.portfolioNotification(PortfolioType.REJECTED, portfolio);

        fcmService.sendNotification(memberId, payload);
      }

    } else {
      log.error("유효하지않은 PortfolioType이 요청되었습니다: {}", request.getPortfolioType());
      throw new CustomException(ErrorCode.INVALID_PORTFOLIO_TYPE);
    }
    return portfolio.getPortfolioId();
  }
}
