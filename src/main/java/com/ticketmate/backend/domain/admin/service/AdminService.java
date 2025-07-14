package com.ticketmate.backend.domain.admin.service;

import static com.ticketmate.backend.global.util.common.CommonUtil.nvl;

import com.ticketmate.backend.domain.admin.dto.request.ConcertHallInfoEditRequest;
import com.ticketmate.backend.domain.admin.dto.request.ConcertHallInfoRequest;
import com.ticketmate.backend.domain.admin.dto.request.ConcertInfoEditRequest;
import com.ticketmate.backend.domain.admin.dto.request.ConcertInfoRequest;
import com.ticketmate.backend.domain.admin.dto.request.PortfolioFilteredRequest;
import com.ticketmate.backend.domain.admin.dto.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.domain.admin.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.domain.admin.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.repository.ConcertRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepositoryCustom;
import com.ticketmate.backend.domain.concert.service.ConcertDateService;
import com.ticketmate.backend.domain.concert.service.ConcertService;
import com.ticketmate.backend.domain.concert.service.TicketOpenDateService;
import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import com.ticketmate.backend.domain.concerthall.domain.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.concerthall.repository.ConcertHallRepository;
import com.ticketmate.backend.domain.concerthall.repository.ConcertHallRepositoryCustom;
import com.ticketmate.backend.domain.concerthall.service.ConcertHallService;
import com.ticketmate.backend.domain.member.service.MemberService;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayloadRequest;
import com.ticketmate.backend.domain.notification.service.FcmService;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.portfolio.repository.PortfolioRepository;
import com.ticketmate.backend.domain.portfolio.repository.PortfolioRepositoryCustom;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.file.constant.UploadType;
import com.ticketmate.backend.global.file.service.FileService;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.util.common.FileUtil;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

  private final ConcertService concertService;
  private final ConcertHallService concertHallService;
  private final ConcertHallRepository concertHallRepository;
  private final ConcertHallRepositoryCustom concertHallRepositoryCustom;
  private final ConcertRepository concertRepository;
  private final ConcertRepositoryCustom concertRepositoryCustom;
  private final ConcertDateService concertDateService;
  private final TicketOpenDateService ticketOpenDateService;
  private final PortfolioRepository portfolioRepository;
  private final PortfolioRepositoryCustom portfolioRepositoryCustom;
  private final FileService fileService;
  private final EntityMapper entityMapper;
  private final FcmService fcmService;
  private final NotificationUtil notificationUtil;
  private final MemberService memberService;

  /*
  ======================================공연======================================
   */

  /**
   * 공연 정보 저장
   *
   * @param request concertName 공연 제목
   *                concertHallId 공연장 PK
   *                concertType 공연 카테고리
   *                concertThumbNail 공연 썸네일 이미지
   *                seatingChart 좌석 배치도 이미지
   *                ticketReservationSite 티켓 예매처 사이트
   *                concertDateRequestList 공연 날짜 DTO List
   *                ticketOpenDateRequestList 티켓 오픈일 DTO List
   */
  @Transactional
  public void saveConcert(ConcertInfoRequest request) {

    // 중복된 공연이름 검증
    validateConcertName(request.getConcertName());

    // 공연장 검색 (요청된 공연장 PK가 null이 아닌 경우)
    ConcertHall concertHall = request.getConcertHallId() != null
        ? concertHallService.findConcertHallById(request.getConcertHallId())
        : null;

    // 공연 썸네일 이미지 저장
    String concertThumbnailUrl = fileService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);

    // 좌석 배치도 이미지 저장
    String seatingChartUrl = !FileUtil.isNullOrEmpty(request.getSeatingChart())
        ? fileService.uploadFile(request.getSeatingChart(), UploadType.CONCERT)
        : null;

    // 공연 정보 저장
    concertRepository.save(createConcertEntity(request, concertHall, concertThumbnailUrl, seatingChartUrl));

    // 공연 날짜 검증 및 저장
    concertDateService.validateConcertDateList(request.getConcertDateRequestList());
    concertDateService.saveConcertDateList(request.getConcertDateRequestList());

    // 티켓 오픈일 검증 및 저장
    ticketOpenDateService.validateTicketOpenDateList(request.getTicketOpenDateRequestList());
    ticketOpenDateService.saveTicketOpenDateList(request.getTicketOpenDateRequestList());

    log.debug("공연 정보 저장 성공: {}", request.getConcertName());
  }

  /**
   * 관리자 공연 필터링 조회
   *
   * @param request concertName 공연이름 [검색어]
   *                concertHallName 공연장이름 [검색어]
   *                concertType 공연 카테고리
   *                ticketReservationSite 예매처
   */
  @Transactional(readOnly = true)
  public Page<ConcertFilteredResponse> filteredConcert(ConcertFilteredRequest request) {
    return concertRepositoryCustom.filteredConcertForAdmin(
        request.getConcertName(),
        request.getConcertHallName(),
        request.getConcertType(),
        request.getTicketReservationSite(),
        request.toPageable()
    );
  }

  /**
   * 관리자 공연 상세 조회
   *
   * @param concertId 공연PK
   * @return 공연 상세 정보
   */
  @Transactional(readOnly = true)
  public ConcertInfoResponse getConcertInfo(UUID concertId) {
    return concertRepositoryCustom.findConcertInfoResponseByConcertIdForAdmin(concertId);
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
    Concert concert = concertService.findConcertById(concertId);

    // 공연명 업데이트
    if (!nvl(request.getConcertName(), "").isEmpty()) {
      // 공연명 중복 검증
      validateConcertName(request.getConcertName());
      log.debug("공연명 업데이트: {}", request.getConcertName());
      concert.setConcertName(request.getConcertName());
    }

    // 공연장 업데이트
    if (request.getConcertHallId() != null) {
      ConcertHall concertHall = concertHallService.findConcertHallById(request.getConcertHallId());
      log.debug("공연장 업데이트: {}", concertHall.getConcertHallName());
      concert.setConcertHall(concertHall);
    }

    // 공연 카테고리 업데이트
    if (request.getConcertType() != null) {
      log.debug("공연 카테고리 업데이트: {}", request.getConcertType());
      concert.setConcertType(request.getConcertType());
    }

    // 공연 썸네일 이미지 업데이트
    if (!FileUtil.isNullOrEmpty(request.getConcertThumbNail())) {
      String newThumbnailUrl = fileService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);
      log.debug("공연 썸네일 이미지 업데이트: {}", newThumbnailUrl);
      concert.setConcertThumbnailUrl(newThumbnailUrl);
    }

    // 좌석 배치도 업데이트
    if (!FileUtil.isNullOrEmpty(request.getSeatingChart())) {
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
    if (!CommonUtil.nullOrEmpty(request.getConcertDateRequestList())) {
      concertDateService.replaceAllConcertDateList(concert, request.getConcertDateRequestList());
    }

    // 티켓 오픈일 업데이트 (기존 데이터 삭제 후 새 데이터 추가)
    if (!CommonUtil.nullOrEmpty(request.getTicketOpenDateRequestList())) {
      ticketOpenDateService.replaceAllTicketOpenDateList(concert, request.getTicketOpenDateRequestList());
    }

    // 공연 정보 저장
    concertRepository.save(concert);
    log.debug("공연 정보 수정 성공: concertId={}, concertName={}", concert.getConcertId(), concert.getConcertName());
  }

  // 중복된 공연명 검증
  private void validateConcertName(String concertName) {
    if (concertRepository.existsByConcertName(concertName)) {
      log.error("중복된 공연명 입니다. 요청된 공연 제목: {}", concertName);
      throw new CustomException(ErrorCode.DUPLICATE_CONCERT_NAME);
    }
  }

  /**
   * Concert 엔티티 생성&반환
   *
   * @param request             concertInfoRequest
   * @param concertHall         공연장 엔티티 or null
   * @param concertThumbnailUrl 공연 썸네일 url
   * @param seatingChartUrl     좌석 배치도 url or null
   * @return Concert 엔티티
   */
  private Concert createConcertEntity(ConcertInfoRequest request, ConcertHall concertHall, String concertThumbnailUrl, String seatingChartUrl) {
    return Concert.builder()
        .concertName(request.getConcertName())
        .concertHall(concertHall)
        .concertType(request.getConcertType())
        .concertThumbnailUrl(concertThumbnailUrl)
        .seatingChartUrl(seatingChartUrl)
        .ticketReservationSite(request.getTicketReservationSite())
        .build();
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

  /*
  ======================================포트폴리오======================================
   */

  /**
   * 페이지당 N개씩(기본10개) 반환합니다
   * 기본 정렬기준: 최신순
   */
  @Transactional(readOnly = true)
  public Page<PortfolioFilteredAdminResponse> filteredPortfolio(PortfolioFilteredRequest request) {
    return portfolioRepositoryCustom.filteredPortfolio(
        request.getUsername(),
        request.getNickname(),
        request.getName(),
        request.getPortfolioType(),
        request.toPageable()
    );
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

      memberService.promoteToAgent(portfolio.getMember());

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
