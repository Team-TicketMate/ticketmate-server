package com.ticketmate.backend.service.concert;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepositoryImpl;
import com.ticketmate.backend.util.common.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertRepositoryImpl concertRepositoryImpl;
    private final EntityMapper entityMapper;

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
}
