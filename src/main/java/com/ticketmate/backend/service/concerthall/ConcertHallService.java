package com.ticketmate.backend.service.concerthall;

import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallInfoRequest;
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

import static com.ticketmate.backend.util.common.CommonUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertHallService {

    private final ConcertHallRepository concertHallRepository;
    private final EntityMapper mapper;

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
        City city = determineCityFromAddress(request.getAddress());

        log.debug("공연장 정보 저장: {}", request.getConcertHallName());
        concertHallRepository.save(ConcertHall.builder()
                .concertHallName(request.getConcertHallName())
                .capacity(request.getCapacity())
                .address(request.getAddress())
                .city(city)
                .concertHallUrl(request.getConcertHallUrl())
                .build());
    }

    /**
     * 공연장 정보 필터링 로직
     *
     * 필터링 조건: 공연장 이름 (검색어), 수용 인원(범위), 도시
     * 정렬 조건: created_date, capacity
     *
     * @param request concertHallName 공연장 이름 검색어 (빈 문자열인 경우 필터링 제외)
     *                maxCapacity 최대 수용인원 (0 인 경우 필터링 제외)
     *                minCapacity (0 인 경우 필터링 제외)
     *                city 도시 (빈 문자열인 경우 필터링 제외)
     *                pageNumber 요청 페이지 번호 (기본 0)
     *                pageSize 한 페이지 당 항목 수 (기본 30)
     *                sortField 정렬할 필드 (기본: created_date)
     *                sortDirection 정렬 방향 (기본: DESC)
     */
    @Transactional(readOnly = true)
    public Page<ConcertHallFilteredResponse> filteredConcertHall(ConcertHallFilteredRequest request) {

        // String, Integer 값 검증
        String concertHallName = nvl(request.getConcertHallName(), "");
        int maxCapacity = null2ZeroInt(request.getMaxCapacity());
        int minCapacity = null2ZeroInt(request.getMinCapacity());
        String city = nvl(String.valueOf(request.getCity()), "");

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
                .filteredConcertHall(
                        concertHallName,
                        maxCapacity,
                        minCapacity,
                        city,
                        pageable);

        // 엔티티를 DTO로 변환하여 Page 객체로 매핑
        return concertHallPage.map(mapper::toConcertHallFilteredResponse);
    }

    /**
     * 주소에 해당하는 city를 반환합니다.
     *
     * @param address 주소
     */
    private City determineCityFromAddress(String address) {
        for (City city : City.values()) {
            if (address.contains(city.getDescription())) {
                log.debug("입력된 주소에 해당하는 city: {}", city.getDescription());
                return city;
            }
        }
        log.error("입력된 주소에 일치하는 city를 찾을 수 없습니다.");
        throw new CustomException(ErrorCode.CITY_NOT_FOUND);
    }
}
