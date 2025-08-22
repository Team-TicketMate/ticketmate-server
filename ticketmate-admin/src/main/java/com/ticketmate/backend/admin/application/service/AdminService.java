package com.ticketmate.backend.admin.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;

import com.ticketmate.backend.admin.application.dto.request.ConcertHallInfoEditRequest;
import com.ticketmate.backend.admin.application.dto.request.ConcertHallInfoRequest;
import com.ticketmate.backend.admin.application.dto.request.ConcertInfoEditRequest;
import com.ticketmate.backend.admin.application.dto.request.ConcertInfoRequest;
import com.ticketmate.backend.admin.application.dto.request.PortfolioFilteredRequest;
import com.ticketmate.backend.admin.application.dto.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.admin.application.dto.response.CoolSmsBalanceResponse;
import com.ticketmate.backend.admin.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.application.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.admin.application.mapper.PortfolioAdminMapper;
import com.ticketmate.backend.admin.core.domain.event.PortfolioHandledEvent;
import com.ticketmate.backend.admin.infrastructure.repository.PortfolioRepositoryCustom;
import com.ticketmate.backend.ai.application.service.VertexAiEmbeddingService;
import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concert.application.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.service.ConcertService;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertRepository;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertRepositoryCustom;
import com.ticketmate.backend.concerthall.application.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.concerthall.application.service.ConcertHallService;
import com.ticketmate.backend.concerthall.core.constant.City;
import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import com.ticketmate.backend.concerthall.infrastructure.repository.ConcertHallRepository;
import com.ticketmate.backend.concerthall.infrastructure.repository.ConcertHallRepositoryCustom;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.application.type.PortfolioNotificationType;
import com.ticketmate.backend.notification.core.service.NotificationService;
import com.ticketmate.backend.portfolio.application.service.PortfolioService;
import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.service.StorageService;
import com.ticketmate.backend.storage.infrastructure.util.FileUtil;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ConcertDateAdminService concertDateAdminService;
  private final TicketOpenDateAdminService ticketOpenDateAdminService;
  private final PortfolioRepositoryCustom portfolioRepositoryCustom;
  @Qualifier("web")
  private final NotificationService notificationService;
  private final StorageService storageService;
  private final PortfolioService portfolioService;
  private final DefaultMessageService messageService;
  private final ApplicationEventPublisher publisher;
  private final PortfolioAdminMapper portfolioAdminMapper;
  private final VertexAiEmbeddingService vertexAiEmbeddingService;

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
  @CacheEvict(cacheNames = "searches", allEntries = true)
  public void saveConcert(ConcertInfoRequest request) {

    // 중복된 공연이름 검증
    validateConcertName(request.getConcertName());

    // 공연장 검색 (요청된 공연장 PK가 null이 아닌 경우)
    ConcertHall concertHall = request.getConcertHallId() != null
        ? concertHallService.findConcertHallById(request.getConcertHallId())
        : null;

    // 공연 썸네일 이미지 저장
    String concertThumbnailUrl = storageService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);

    // 좌석 배치도 이미지 저장
    String seatingChartUrl = !FileUtil.isNullOrEmpty(request.getSeatingChart())
        ? storageService.uploadFile(request.getSeatingChart(), UploadType.CONCERT)
        : null;

    // 공연 정보 저장
    Concert concert = concertRepository.save(createConcertEntity(request, concertHall, concertThumbnailUrl, seatingChartUrl));

    // 공연 날짜 검증 및 저장
    concertDateAdminService.validateConcertDateList(request.getConcertDateRequestList());
    concertDateAdminService.saveConcertDateList(concert, request.getConcertDateRequestList());

    // 티켓 오픈일 검증 및 저장
    ticketOpenDateAdminService.validateTicketOpenDateList(request.getTicketOpenDateRequestList());
    ticketOpenDateAdminService.saveTicketOpenDateList(concert, request.getTicketOpenDateRequestList());

    // 공연 임베딩 저장
    generateOrUpdateConcertEmbedding(concert, concertHall);
    log.debug("공연 정보 및 임베딩 저장 성공: {}", request.getConcertName());
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
  @CacheEvict(cacheNames = "searches", allEntries = true)
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
      String newThumbnailUrl = storageService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);
      log.debug("공연 썸네일 이미지 업데이트: {}", newThumbnailUrl);
      concert.setConcertThumbnailUrl(newThumbnailUrl);
    }

    // 좌석 배치도 업데이트
    if (!FileUtil.isNullOrEmpty(request.getSeatingChart())) {
      String newSeatingChartUrl = storageService.uploadFile(request.getSeatingChart(), UploadType.CONCERT);
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
      concertDateAdminService.replaceAllConcertDateList(concert, request.getConcertDateRequestList());
    }

    // 티켓 오픈일 업데이트 (기존 데이터 삭제 후 새 데이터 추가)
    if (!CommonUtil.nullOrEmpty(request.getTicketOpenDateRequestList())) {
      ticketOpenDateAdminService.replaceAllTicketOpenDateList(concert, request.getTicketOpenDateRequestList());
    }

    // 공연 임베딩 업데이트
    generateOrUpdateConcertEmbedding(concert, concert.getConcertHall());

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

  /**
   * Concert 임베딩 생성 및 저장
   * "공연이름" + "공연카테고리" + "공연장이름"
   *
   * @param concert     공연
   * @param concertHall 공연장
   */
  private void generateOrUpdateConcertEmbedding(Concert concert, ConcertHall concertHall) {
    String embeddingText = CommonUtil.combineTexts(
        concert.getConcertName(),
        concert.getConcertType().getDescription(),
        concertHall != null ? concertHall.getConcertHallName() : null
    );
    vertexAiEmbeddingService.fetchOrGenerateEmbedding(
        concert.getConcertId(),
        embeddingText,
        EmbeddingType.CONCERT
    );
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
   * @param portfolioId 포트폴리오 PK
   */
  @Transactional
  public PortfolioForAdminResponse getPortfolio(UUID portfolioId) {
    Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);

    // 포트폴리오가 "검토 대기" 상태인 경우
    if (portfolio.getPortfolioType().equals(PortfolioType.PENDING_REVIEW)) {
      // 포트폴리오 상태 "검토중" (IN_REVIEW)으로 변경
      portfolio.setPortfolioType(PortfolioType.REVIEWING);
    }
    return portfolioAdminMapper.toPortfolioForAdminResponse(portfolio);
  }

  /**
   * 관리자의 포트폴리오 승인 및 반려처리 로직
   *
   * @param portfolioId 포트폴리오 PK
   * @param request     portfolioId (UUID)
   *                    PortfolioType (포트폴리오 상태)
   */
  @Transactional
  public void reviewPortfolioCompleted(UUID portfolioId, PortfolioStatusUpdateRequest request) {
    Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
    Member client = portfolio.getMember();
    handlePortfolio(client, portfolio, request.getPortfolioType());
  }

  /**
   * 포트폴리오 알림 payload 생성
   */
  private NotificationPayload buildPortfolioNotificationPayload(Member member, PortfolioType portfolioType) {
    PortfolioNotificationType notificationType =
        CommonUtil.stringToEnum(PortfolioNotificationType.class, portfolioType.name());
    return notificationType.toPayload(member.getNickname());
  }

  /**
   * 포트폴리오 수락 or 거절
   *
   * @param member        회원
   * @param portfolio     포트폴리오
   * @param portfolioType 변경하려는 포트폴리오 상태
   */
  private void handlePortfolio(Member member, Portfolio portfolio, PortfolioType portfolioType) {
    if (!portfolio.getPortfolioType().equals(PortfolioType.REVIEWING)) {
      log.error("검토중인 상태의 포트폴리오만 승인 및 반려처리 가능합니다. 요청된 포트폴리오 상태: {}", portfolio.getPortfolioType());
      throw new CustomException(ErrorCode.INVALID_PORTFOLIO_TYPE);
    }

    // 포트폴리오 상태 업데이트
    portfolio.setPortfolioType(portfolioType);

    // '승인'인 경우 의뢰인 -> 대리인 변경
    if (portfolioType.equals(PortfolioType.APPROVED)) {
      portfolioService.promoteToAgent(portfolio);
      publisher.publishEvent(new PortfolioHandledEvent(portfolio.getPortfolioId(), portfolioType));
    }

    NotificationPayload payload = buildPortfolioNotificationPayload(member, portfolioType);

    notificationService.sendToMember(member.getMemberId(), payload);
    log.debug("포트폴리오: {}, {} 완료: {}", portfolio.getPortfolioId(), portfolioType.getDescription(), portfolioType);
  }

  /*
  ======================================SMS======================================
   */

  /**
   * CoolSMS 잔액 조회
   */
  public CoolSmsBalanceResponse getBalance() {
    Balance balance = messageService.getBalance();
    return CoolSmsBalanceResponse.builder()
        .balance(balance.getBalance() != null ? balance.getBalance() : 0f)
        .point(balance.getPoint() != null ? balance.getPoint() : 0f)
        .build();
  }
}
