package com.ticketmate.backend.service;

import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.ConcertHallInfoRequest;
import com.ticketmate.backend.object.postgres.ConcertHall;
import com.ticketmate.backend.repository.postgres.ConcertHallRepository;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertHallService {

    private final ConcertHallRepository concertHallRepository;

    /**
     * 공연장 정보 저장
     * 관리자만 저장 가능합니다
     */
    @Transactional
    public ApiResponse<Void> saveHallInfo(ConcertHallInfoRequest request) {

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
        return ApiResponse.success(null);
    }

    /**
     * 주소에 해당하는 city를 반환합니다.
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
