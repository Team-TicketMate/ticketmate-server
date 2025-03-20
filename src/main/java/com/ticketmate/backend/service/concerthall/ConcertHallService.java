package com.ticketmate.backend.service.concerthall;

import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallInfoResponse;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.repository.postgres.concerthall.ConcertHallRepository;
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

import java.util.UUID;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertHallService {

    private final ConcertHallRepository concertHallRepository;
    private final EntityMapper entityMapper;

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
    public Page<ConcertHallFilteredResponse> filteredConcertHall(ConcertHallFilteredRequest request) {

        // String, Integer 값 검증
        String concertHallName = nvl(request.getConcertHallName(), "");
        City city = null;

        // 지역 코드에 해당하는 City 반환
        if (request.getCityCode() != null) {
            city = City.fromCityCode(request.getCityCode());
        }

        // 정렬 조건
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDirection()),
                request.getSortField());

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(
                request.getPageNumber(),
                request.getPageSize(),
                sort
        );

        Page<ConcertHall> concertHallPage = concertHallRepository
                .filteredConcertHall(concertHallName, city, pageable);

        // 엔티티를 DTO로 변환하여 Page 객체로 매핑
        return concertHallPage.map(entityMapper::toConcertHallFilteredResponse);
    }

    /**
     * 공연장 정보 상세조회
     */
    @Transactional(readOnly = true)
    public ConcertHallInfoResponse getConcertHallInfo(UUID concertHallId) {

        ConcertHall concertHall = concertHallRepository.findById(concertHallId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND));

        return entityMapper.toConcertHallInfoResponse(concertHall);
    }
}
