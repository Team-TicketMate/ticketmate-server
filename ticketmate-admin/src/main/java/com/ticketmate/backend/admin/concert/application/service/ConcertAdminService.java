package com.ticketmate.backend.admin.concert.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;

import com.ticketmate.backend.admin.concert.application.dto.request.ConcertInfoEditRequest;
import com.ticketmate.backend.admin.concert.application.dto.request.ConcertInfoRequest;
import com.ticketmate.backend.ai.application.service.VertexAiEmbeddingService;
import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concert.application.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.dto.view.ConcertFilteredInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertInfo;
import com.ticketmate.backend.concert.application.mapper.ConcertMapper;
import com.ticketmate.backend.concert.application.service.ConcertService;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertRepository;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertRepositoryCustom;
import com.ticketmate.backend.concerthall.application.service.ConcertHallService;
import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import com.ticketmate.backend.storage.core.service.StorageService;
import com.ticketmate.backend.storage.infrastructure.util.FileUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertAdminService {

  private final ConcertService concertService;
  private final ConcertHallService concertHallService;
  private final ConcertRepository concertRepository;
  private final ConcertRepositoryCustom concertRepositoryCustom;
  private final ConcertDateAdminService concertDateAdminService;
  private final TicketOpenDateAdminService ticketOpenDateAdminService;
  private final StorageService storageService;
  private final VertexAiEmbeddingService vertexAiEmbeddingService;
  private final ConcertMapper concertMapper;

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
    FileMetadata concertThumbnailMetadata = storageService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);
    String concertThumbnailStoredPath = concertThumbnailMetadata.storedPath();

    // 좌석 배치도 이미지 저장
    String seatingChartStoredPath = null;
    if (!FileUtil.isNullOrEmpty(request.getSeatingChart())) {
      FileMetadata seatingChartMetadata = storageService.uploadFile(request.getSeatingChart(), UploadType.CONCERT);
      seatingChartStoredPath = seatingChartMetadata.storedPath();
    }

    // 공연 정보 저장
    Concert concert = concertRepository.save(createConcertEntity(request, concertHall, concertThumbnailStoredPath, seatingChartStoredPath));

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
    Page<ConcertFilteredInfo> concertFilteredInfoPage = concertRepositoryCustom.filteredConcertForAdmin(
        request.getConcertName(),
        request.getConcertHallName(),
        request.getConcertType(),
        request.getTicketReservationSite(),
        request.toPageable()
    );
    return concertMapper.toConcertFilteredResponsePage(concertFilteredInfoPage);
  }

  /**
   * 관리자 공연 상세 조회
   *
   * @param concertId 공연PK
   * @return 공연 상세 정보
   */
  @Transactional(readOnly = true)
  public ConcertInfoResponse getConcertInfo(UUID concertId) {
    ConcertInfo concertInfo = concertRepositoryCustom.findConcertInfoByConcertIdForAdmin(concertId);
    return concertMapper.toConcertInfoResponse(concertInfo);
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
      storageService.deleteFile(concert.getConcertThumbnailStoredPath());
      FileMetadata newThumbnailMetadata = storageService.uploadFile(request.getConcertThumbNail(), UploadType.CONCERT);
      String newThumbnailStoredPath = newThumbnailMetadata.storedPath();
      log.debug("공연 썸네일 이미지 업데이트: {}", newThumbnailStoredPath);
      concert.setConcertThumbnailStoredPath(newThumbnailStoredPath);
    }

    // 좌석 배치도 업데이트
    if (!FileUtil.isNullOrEmpty(request.getSeatingChart())) {
      storageService.deleteFile(concert.getSeatingChartStoredPath());
      FileMetadata newSeatingChartMetadata = storageService.uploadFile(request.getSeatingChart(), UploadType.CONCERT);
      String newSeatingChartStoredPath = newSeatingChartMetadata.storedPath();
      log.debug("좌석 배치도 이미지 업데이트: {}", newSeatingChartStoredPath);
      concert.setSeatingChartStoredPath(newSeatingChartStoredPath);
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
   * @param request                    concertInfoRequest
   * @param concertHall                공연장 엔티티 or null
   * @param concertThumbnailStoredPath 공연 썸네일 저장 경로
   * @param seatingChartStoredPath     좌석 배치도 저장 경로 || null
   * @return Concert 엔티티
   */
  private Concert createConcertEntity(ConcertInfoRequest request, ConcertHall concertHall, String concertThumbnailStoredPath, String seatingChartStoredPath) {
    return Concert.builder()
        .concertName(request.getConcertName())
        .concertHall(concertHall)
        .concertType(request.getConcertType())
        .concertThumbnailStoredPath(concertThumbnailStoredPath)
        .seatingChartStoredPath(seatingChartStoredPath)
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
}
