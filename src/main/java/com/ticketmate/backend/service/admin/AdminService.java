package com.ticketmate.backend.service.admin;

import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.dto.admin.request.PortfolioSearchRequest;
import com.ticketmate.backend.object.dto.admin.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.dto.concert.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallInfoRequest;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concerthall.ConcertHallRepository;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioRepository;
import com.ticketmate.backend.repository.redis.FcmTokenRepository;
import com.ticketmate.backend.service.fcm.FcmService;
import com.ticketmate.backend.service.file.FileService;
import com.ticketmate.backend.util.common.EntityMapper;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final ConcertHallRepository concertHallRepository;
    private final ConcertRepository concertRepository;
    private final PortfolioRepository portfolioRepository;
    private final FileService fileService;
    private final EntityMapper entityMapper;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmService fcmService;
    @Value("${cloud.aws.s3.path.portfolio.cloud-front-domain}")
    private String portFolioDomain;


    /*
    ======================================공연======================================
     */

    /**
     * 콘서트 정보 저장
     *
     * @param request concertName 공연 제목
     *                concertHallName 공연장 이름
     *                concertType 공연 카테고리
     *                ticketPreOpenDate 선구매 오픈일
     *                ticketOpenDate 티켓 구매 오픈일
     *                duration 공연 시간 (분)
     *                session 공연 회차
     *                concertThumbnailUrl 공연 썸네일
     *                ticketReservationSite 티켓 예매처 사이트
     */
    @Transactional
    public void saveConcertInfo(ConcertInfoRequest request) {

        // 중복된 공연이름 검증
        if (concertRepository.existsByConcertName(request.getConcertName())) {
            log.error("중복된 공연 제목입니다. 요청된 공연 제목: {}", request.getConcertName());
            throw new CustomException(ErrorCode.DUPLICATE_CONCERT_NAME);
        }

        // 공연장 검색
        ConcertHall concertHall = concertHallRepository.findByConcertHallName(request.getConcertHallName())
                .orElseThrow(() -> {
                    log.error("{} 에 해당하는 공연장 정보를 찾을 수 없습니다.", request.getConcertHallName());
                    return new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND);
                });

        // 콘서트 썸네일 저장
        String concertThumbnailUrl = fileService
                .saveFile(request.getConcertThumbNail());

        // 콘서트 정보 저장
        concertRepository.save(Concert.builder()
                .concertName(request.getConcertName())
                .concertHall(concertHall)
                .concertType(request.getConcertType())
                .ticketPreOpenDate(request.getTicketPreOpenDate())
                .ticketOpenDate(request.getTicketOpenDate())
                .duration(request.getDuration())
                .session(request.getSession())
                .concertThumbnailUrl(concertThumbnailUrl)
                .ticketReservationSite(request.getTicketReservationSite())
                .build());
        log.debug("공연 정보 저장 성공: {}", request.getConcertName());
    }

    /*
    ======================================공연장======================================
     */

    /**
     * 공연장 정보 저장
     * 관리자만 저장 가능합니다
     */
    @Transactional
    public void saveHallInfo(ConcertHallInfoRequest request) {

        // 중복된 공연장이름 검증
        if (concertHallRepository.existsByConcertHallName(request.getConcertHallName())) {
            log.error("중복된 공연장 이름입니다. 요청된 공연장 이름: {}", request.getConcertHallName());
            throw new CustomException(ErrorCode.DUPLICATE_CONCERT_HALL_NAME);
        }

        // 요청된 주소에 맞는 city할당
        City city = City.determineCityFromAddress(request.getAddress());

        log.debug("공연장 정보 저장: {}", request.getConcertHallName());
        concertHallRepository.save(ConcertHall.builder()
                .concertHallName(request.getConcertHallName())
                .capacity(request.getCapacity())
                .address(request.getAddress())
                .city(city)
                .concertHallUrl(request.getConcertHallUrl())
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
     * @param portfolioId (UUID)
     */
    @Transactional
    public PortfolioForAdminResponse getPortfolio(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 검토중으로 update
        portfolio.setPortfolioType(PortfolioType.REVIEWING);

        UUID memberId = portfolio.getMember().getMemberId();

        String notificationMessage = reviewingNotificationMessage(portfolio);
        fcmService.sendNotification(memberId, notificationMessage);

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
     * @param portfolioId (UUID)
     *        PortfolioType (포트폴리오 상태)
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

            String notificationMessage = reviewCompletedNotificationMessage(portfolio);
            fcmService.sendNotification(memberId, notificationMessage);
        } else {
            // 반려
            portfolio.setPortfolioType(PortfolioType.COMPANION);
            log.debug("반려완료: {}", portfolio.getPortfolioType());

            String notificationMessage = companionNotificationMessage(portfolio);
            fcmService.sendNotification(memberId, notificationMessage);
        }

        return portfolio.getPortfolioId();
    }

    /**
     * 검토중일때 전송될 알림입니다.
     */
    private String reviewingNotificationMessage(Portfolio portfolio) {
        return "관리자가 " + portfolio.getMember().getNickname() + " 님의 포트폴리오를 검토중입니다.";
    }
    /**
     * 승인시 전송될 알림입니다.
     */

    private String reviewCompletedNotificationMessage(Portfolio portfolio) {
        return portfolio.getMember().getNickname() + " 님의 포트폴리오가 관리자에 의해 승인처리 되었습니다.";
    }
    /**
     * 반려시 전송될 알림입니다.
     */

    private String companionNotificationMessage(Portfolio portfolio) {
        return portfolio.getMember().getNickname() + " 님의 포트폴리오가 관리자에 의해 반려처리 되었습니다.";
    }
}
