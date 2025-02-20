package com.ticketmate.backend.service.concert;

import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concerthall.ConcertHallRepository;
import com.ticketmate.backend.service.file.FileService;
import com.ticketmate.backend.util.common.EntityMapper;
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

import static com.ticketmate.backend.util.common.CommonUtil.null2ZeroInt;
import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertHallRepository concertHallRepository;
    private final FileService fileService;
    private final EntityMapper mapper;

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
                    return new CustomException(ErrorCode.CONCERT_HALL_NAME_NOT_FOUND);
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

    /**
     * 콘서트 필터링 로직
     * <p>
     * 필터링 조건: 콘서트 제목 (검색어), 공연장 이름 (검색어), 공연 카테고리, 선예매 오픈일 범위, 티켓 오픈일 범위, 회차
     * 정렬 조건: created_date, ticket_pre_open_date, ticket_open_date, duration
     *
     * @param request concertName 콘서트 제목 검색어 (빈 문자열인 경우 필터링 제외)
     *                concertHallName 공연장 이름 검색어 (빈 문자열인 경우 필터링 제외)
     *                concertType 공연 카테고리 (빈 문자열인 경우 필터링 제외)
     *                ticketPreOpenStartDate 선예매 오픈 범위 시작일 (비어있는 경우 필터링 제외)
     *                ticketPreOpenEndDate 선예매 오픈 범위 종료일 (비어있는 경우 필터링 제외)
     *                ticketOpenStartDate 티켓 오픈 범위 시작일 (비어있는 경우 필터링 제외)
     *                ticketPreOpenEndDate 티켓 오픈 범위 종료일 (비어있는 경우 필터링 제외)
     *                session 회차 (0 인 경우 필터링 제외)
     *                ticketReservationSite 예매처 (빈 문자열인 경우 필터링 제외)
     *                pageNumber 요청 페이지 번호 (기본 0)
     *                pageSize 한 페이지 당 항목 수 (기본 30)
     *                sortField 정렬할 필드 (기본: created_date)
     *                sortDirection 정렬 방향 (기본: DESC)
     */
    @Transactional(readOnly = true)
    public Page<ConcertFilteredResponse> filteredConcert(ConcertFilteredRequest request) {

        // String, Integer 값 검증
        String concertName = nvl(request.getConcertName(), "");
        String concertHallName = nvl(request.getConcertHallName(), "");
        String concertType = nvl(String.valueOf(request.getConcertType()), "");
        int session = null2ZeroInt(request.getSession());
        String ticketReservationSite = nvl(String.valueOf(request.getTicketReservationSite()), "");

        // LocalDateTime 범위 검증
        if (request.getTicketPreOpenStartDate() != null && request.getTicketPreOpenEndDate() != null) {
            if (request.getTicketPreOpenStartDate().isAfter(request.getTicketPreOpenEndDate())) {
                log.error("선예매 필터링 범위가 잘못 입력되었습니다.");
                throw new CustomException(ErrorCode.INVALID_RANGE_REQUEST);
            }
        } else if (request.getTicketOpenStartDate() != null && request.getTicketOpenEndDate() != null) {
            if (request.getTicketOpenStartDate().isAfter(request.getTicketOpenEndDate())) {
                log.error("티켓 오픈일 필터링 범위가 잘못 입력되었습니다.");
                throw new CustomException(ErrorCode.INVALID_RANGE_REQUEST);
            }
        }

        // 정렬 조건
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDirection()),
                request.getSortField());

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(
                request.getPageNumber(),
                request.getPageSize()
                , sort
        );

        Page<Concert> concertPage = concertRepository.filteredConcert(
                concertName,
                concertHallName,
                concertType,
                request.getTicketPreOpenStartDate(),
                request.getTicketPreOpenEndDate(),
                request.getTicketOpenStartDate(),
                request.getTicketOpenEndDate(),
                session,
                ticketReservationSite,
                pageable
        );

        // 엔티티를 DTO로 변환하여 Page 객체로 매핑
        return concertPage.map(mapper::toConcertFilteredResponse);
    }
}
