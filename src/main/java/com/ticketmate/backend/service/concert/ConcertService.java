package com.ticketmate.backend.service.concert;

import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
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
    private final EntityMapper entityMapper;

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
        return concertPage.map(entityMapper::toConcertFilteredResponse);
    }
}
