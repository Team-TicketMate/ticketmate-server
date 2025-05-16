package com.ticketmate.backend.service.concert;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.concert.response.ConcertInfoResponse;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import com.ticketmate.backend.repository.postgres.concert.ConcertDateRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepositoryImpl;
import com.ticketmate.backend.repository.postgres.concert.TicketOpenDateRepository;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final TicketOpenDateRepository ticketOpenDateRepository;
    private final ConcertRepositoryImpl concertRepositoryImpl;

    /**
     * 공연 필터링 조회 로직
     *
     * @param request concertName
     *                concertHallName
     *                concertType
     *                ticketReservationSite
     *                pageNumber
     *                pageSize
     *                sortField
     *                sortDirection
     */
    @Transactional(readOnly = true)
    public Page<ConcertFilteredResponse> filteredConcert(ConcertFilteredRequest request) {

        // 1. 요청 값 확인
        String concertName = nvl(request.getConcertName(), "");
        String concertHallName = nvl(request.getConcertHallName(), "");
        ConcertType concertType = request.getConcertType() != null ? request.getConcertType() : null;
        TicketReservationSite ticketReservationSite = request.getTicketReservationSite() != null ? request.getTicketReservationSite() : null;

        // 2. 정렬 조건
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDirection()),
                request.getSortField());

        // 3. Pageable 생성
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(), sort);

        // 4. 데이터베이스 조회
        return concertRepositoryImpl.filteredConcert(
                concertName,
                concertHallName,
                concertType,
                ticketReservationSite,
                pageable
        );
    }

    /**
     * 공연 상세 조회 로직
     *
     * @param concertId 공연 PK
     * @return 공연 상세 정보
     */
    @Transactional(readOnly = true)
    public ConcertInfoResponse getConcertInfo(UUID concertId) {

        // 1. DB에서 공연정보, 공연일자, 티켓오픈일 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        // 공연장 이름 확인
        String concertHallName = null;
        if (concert.getConcertHall() != null) { // 공연장 정보가 존재하는 경우
            concertHallName = concert.getConcertHall().getConcertHallName();
        }

        // 예매처 확인
        TicketReservationSite ticketReservationSite = null;
        if (concert.getTicketReservationSite() != null) {
            ticketReservationSite = concert.getTicketReservationSite();
        }

        // 좌석배치도 URL 확인
        String seatingChartUrl = null;
        if (concert.getSeatingChartUrl() != null) {
            seatingChartUrl = concert.getSeatingChartUrl();
        }

        // 1-1. 비동기적으로 관련 데이터 멀티스레드 조회
        CompletableFuture<List<ConcertDate>> concertDateListFuture = CompletableFuture
                .supplyAsync(() -> concertDateRepository.findAllByConcertConcertId(concert.getConcertId()));
        CompletableFuture<List<TicketOpenDate>> ticketOpenDateListFuture = CompletableFuture
                .supplyAsync(() -> ticketOpenDateRepository.findAllByConcertConcertId(concert.getConcertId()));

        // 1-2. 데이터 조회 완료 대기
        List<ConcertDate> concertDateList;
        List<TicketOpenDate> ticketOpenDateList;
        try {
            concertDateList = concertDateListFuture.get();
            ticketOpenDateList = ticketOpenDateListFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("공연 관련 데이터 멀티스레드 조회 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 2. 공연 시작일/종료일 계산
        LocalDateTime startDate = concertDateList.stream()
                .map(ConcertDate::getPerformanceDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime endDate = concertDateList.stream()
                .map(ConcertDate::getPerformanceDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);


        // 3. 사전/일반 예매 정보 추출
        List<TicketOpenDate> preOpenDateList = new ArrayList<>();
        List<TicketOpenDate> generalOpenDateList = new ArrayList<>();
        for (TicketOpenDate openDate : ticketOpenDateList) {
            if (openDate.getTicketOpenType().equals(TicketOpenType.PRE_OPEN)) {
                preOpenDateList.add(openDate);
            } else if (openDate.getTicketOpenType().equals(TicketOpenType.GENERAL_OPEN)) {
                generalOpenDateList.add(openDate);
            } else {
                log.error("TicketOpen 객체 내부 TicketOpenType이 없는 데이터가 존재합니다.");
                throw new CustomException(ErrorCode.TICKET_OPEN_TYPE_NOT_FOUND);
            }
        }
        if (preOpenDateList.size() > 1) {
            log.error("선예매 오픈일이 여러 개 등록되어있습니다. 등록된 선예매 정보 개수: {}개", preOpenDateList.size());
            throw new CustomException(ErrorCode.PRE_OPEN_COUNT_EXCEED);
        } else if (generalOpenDateList.size() > 1) {
            log.error("일반 예매 오픈일이 여러 개 등록되어있습니다. 등록된 일반예매 정보 개수: {}개", generalOpenDateList.size());
            throw new CustomException(ErrorCode.GENERAL_OPEN_COUNT_EXCEED);
        } else if (preOpenDateList.isEmpty() && generalOpenDateList.isEmpty()) {
            log.error("선예매/일반예매 오픈일 데이터를 찾을 수 없습니다.");
            throw new CustomException(ErrorCode.TICKET_OPEN_DATE_NOT_FOUND);
        }

        LocalDateTime preOpenDate = null;
        Integer preOpenRequestMaxCount = null;
        Boolean preOpenIsBankTransfer = null;
        if (!preOpenDateList.isEmpty()){
            TicketOpenDate preOpen = preOpenDateList.get(0);
            preOpenDate = preOpen != null ? preOpen.getOpenDate() : null;
            preOpenRequestMaxCount = preOpen != null ? preOpen.getRequestMaxCount() : null;
            preOpenIsBankTransfer = preOpen != null ? preOpen.getIsBankTransfer() : null;
        }

        LocalDateTime generalOpenDate = null;
        Integer generalOpenRequestMaxCount = null;
        Boolean generalOpenIsBankTransfer = null;
        if (!generalOpenDateList.isEmpty()) {
            TicketOpenDate generalOpen = generalOpenDateList.get(0);
            generalOpenDate = generalOpen != null ? generalOpen.getOpenDate() : null;
            generalOpenRequestMaxCount = generalOpen != null ? generalOpen.getRequestMaxCount() : null;
            generalOpenIsBankTransfer = generalOpen != null ? generalOpen.getIsBankTransfer() : null;
        }

        // 4. 반환값
        return ConcertInfoResponse.builder()
                .concertName(concert.getConcertName())
                .concertHallName(concertHallName)
                .concertThumbnailUrl(concert.getConcertThumbnailUrl())
                .seatingChartUrl(seatingChartUrl)
                .concertType(concert.getConcertType())
                .startDate(startDate)
                .endDate(endDate)
                .preOpenDate(preOpenDate)
                .preOpenRequestMaxCount(preOpenRequestMaxCount)
                .preOpenIsBankTransfer(preOpenIsBankTransfer)
                .generalOpenDate(generalOpenDate)
                .generalOpenRequestMaxCount(generalOpenRequestMaxCount)
                .generalOpenIsBankTransfer(generalOpenIsBankTransfer)
                .ticketReservationSite(ticketReservationSite)
                .build();
    }
}
