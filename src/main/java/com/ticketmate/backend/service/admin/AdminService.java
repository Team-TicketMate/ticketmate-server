package com.ticketmate.backend.service.admin;

import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.dto.admin.request.PortfolioSearchRequest;
import com.ticketmate.backend.object.dto.admin.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.object.dto.admin.response.ConcertHallFilteredAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.dto.concert.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.concert.request.TicketOpenDateRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallInfoRequest;
import com.ticketmate.backend.object.dto.notification.request.NotificationPayloadRequest;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.repository.postgres.concert.ConcertDateRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concert.TicketOpenDateRepository;
import com.ticketmate.backend.repository.postgres.concerthall.ConcertHallRepository;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioRepository;
import com.ticketmate.backend.service.concerthall.ConcertHallService;
import com.ticketmate.backend.service.fcm.FcmService;
import com.ticketmate.backend.service.file.FileService;
import com.ticketmate.backend.util.common.EntityMapper;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import com.ticketmate.backend.util.notification.NotificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

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
    private final View error;
    @Value("${cloud.aws.s3.path.portfolio.cloud-front-domain}")
    private String portFolioDomain;


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
        if (concertRepository.existsByConcertName(request.getConcertName())) {
            log.error("중복된 공연 제목입니다. 요청된 공연 제목: {}", request.getConcertName());
            throw new CustomException(ErrorCode.DUPLICATE_CONCERT_NAME);
        }

        // 2. 공연장 검색 (요청된 공연장 PK가 null이 아닌 경우)
        ConcertHall concertHall = null;
        if (request.getConcertHallId() != null) {
            concertHall = concertHallRepository.findById(request.getConcertHallId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND));
        }

        // 3. 콘서트 썸네일 저장
        String concertThumbnailUrl = fileService
                .saveFile(request.getConcertThumbNail());

        // 4. 좌석 배치도 저장
        String seatingChartUrl = null;
        if (request.getSeatingChart() != null) {
            seatingChartUrl = fileService.saveFile(request.getSeatingChart());
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

        // 6. 공연 날짜 저장
        if (!request.getConcertDateRequests().isEmpty()) {
            List<ConcertDate> concertDates = request.getConcertDateRequests().stream()
                    .map(dateRequest -> ConcertDate.builder()
                            .concert(concert)
                            .performanceDate(dateRequest.getPerformanceDate())
                            .session(dateRequest.getSession())
                            .build()
                    )
                    .collect(Collectors.toList());
            concertDateRepository.saveAll(concertDates);
        }

        // 7. 티켓 오픈일 검증 및 저장
        if (!request.getTicketOpenDateRequests().isEmpty()) { // 티켓 오픈일이 입력된 경우
            // 티켓 오픈일 요청에 일반 예매 오픈일이 있는지 확인
            validateTicketOpenDates(request.getTicketOpenDateRequests());
            List<TicketOpenDate> ticketOpenDates = request.getTicketOpenDateRequests().stream()
                    .map(ticketOpenDateRequest -> TicketOpenDate.builder()
                            .concert(concert)
                            .openDate(ticketOpenDateRequest.getOpenDate())
                            .requestMaxCount(ticketOpenDateRequest.getRequestMaxCount())
                            .isBankTransfer(ticketOpenDateRequest.getIsBankTransfer())
                            .isPreOpen(ticketOpenDateRequest.getIsPreOpen())
                            .build())
                    .collect(Collectors.toList());
            ticketOpenDateRepository.saveAll(ticketOpenDates);
        }
        log.debug("공연 정보 저장 성공: {}", request.getConcertName());
    }

    /**
     * 공연장 정보 필터링 로직
     *
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
     * 티켓 오픈일 검증
     */
    private void validateTicketOpenDates(List<TicketOpenDateRequest> ticketOpenDateRequests) {

        // 일반 예매 필수 검증
        boolean hasGeneralOpen = ticketOpenDateRequests.stream()
                .anyMatch(date -> !date.getIsPreOpen());
        if (!hasGeneralOpen) {
            log.error("일반 예매 날짜는 필수로 포함되어야합니다");
            throw new CustomException(ErrorCode.GENERAL_TICKET_OPEN_DATE_REQUIRED);
        }
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
    public void saveHallInfo(ConcertHallInfoRequest request) {

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

    /*
    ======================================포트폴리오======================================
     */

    /**
     * 페이지당 10개씩 관리자에게 포트폴리오 리스트 데이터를 보여줍니다.
     * 포트폴리오 정렬기준은 오래된순으로 정렬됩니다.
     */
    @Transactional(readOnly = true)
    public Page<PortfolioListForAdminResponse> getPortfolioList(PortfolioSearchRequest request) {

        int pageIndex = Math.max(1, request.getIndex()) - 1;

        Pageable pageable = PageRequest.of(pageIndex,
                10,
                Sort.by("createdDate").ascending());

        Page<Portfolio> portfolioPage = portfolioRepository.findAllByPortfolioType(PortfolioType.UNDER_REVIEW, pageable);

        return portfolioPage.map(entityMapper::toPortfolioListForAdminResponse);
    }

    /**
     * 포트폴리오 상세조회 로직
     *
     * @param portfolioId (UUID)
     */
    @Transactional
    public PortfolioForAdminResponse getPortfolio(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 검토중으로 update
        portfolio.setPortfolioType(PortfolioType.REVIEWING);

        UUID memberId = portfolio.getMember().getMemberId();

        NotificationPayloadRequest payload = notificationUtil
                .portfolioNotification(PortfolioType.REVIEWING, portfolio);

        fcmService.sendNotification(memberId, payload);

        PortfolioForAdminResponse portfolioForAdminResponse = entityMapper.toPortfolioForAdminResponse(portfolio);

        List<PortfolioImg> imgList = portfolio.getImgList();

        // 이미지 URL 파싱
        List<String> portfolioImgList = imgList.stream().map(img -> portFolioDomain + img.getImgName())
                .toList();

        portfolioForAdminResponse.addPortfolioImg(portfolioImgList);

        return portfolioForAdminResponse;
    }

    /**
     * 관리자의 포트폴리오 승인 및 반려처리 로직
     *
     * @param portfolioId (UUID)
     *                    PortfolioType (포트폴리오 상태)
     */
    @Transactional
    public UUID reviewPortfolioCompleted(UUID portfolioId, PortfolioStatusUpdateRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        log.debug("변경전 포트폴리오 TYPE: {}", portfolio.getPortfolioType());

        UUID memberId = portfolio.getMember().getMemberId();

        // 승인
        if (request.getPortfolioType().equals(PortfolioType.REVIEW_COMPLETED)) {
            portfolio.setPortfolioType(PortfolioType.REVIEW_COMPLETED);
            log.debug("승인완료: {}", portfolio.getPortfolioType());

            portfolio.getMember().setMemberType(MemberType.AGENT);

            NotificationPayloadRequest payload = notificationUtil
                    .portfolioNotification(PortfolioType.REVIEW_COMPLETED, portfolio);

            fcmService.sendNotification(memberId, payload);
        } else {
            // 반려
            portfolio.setPortfolioType(PortfolioType.COMPANION);
            log.debug("반려완료: {}", portfolio.getPortfolioType());

            NotificationPayloadRequest payload = notificationUtil
                    .portfolioNotification(PortfolioType.COMPANION, portfolio);

            fcmService.sendNotification(memberId, payload);
        }

        return portfolio.getPortfolioId();
    }
}
