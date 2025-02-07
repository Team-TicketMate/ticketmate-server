package com.ticketmate.backend.service;

import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.ConcertHallInfoRequest;
import com.ticketmate.backend.object.postgres.ConcertHall;
import com.ticketmate.backend.repository.postgres.ConcertHallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertHallService {

    private final ConcertHallRepository concertHallRepository;

    /**
     * 공연장 정보 저장
     */
    public ApiResponse<Void> saveHallInfo(ConcertHallInfoRequest request) {

        log.debug("공연장 정보 저장: {}", request.getConcertHallName());
        concertHallRepository.save(ConcertHall.builder()
                .concertHallName(request.getConcertHallName())
                .capacity(request.getCapacity())
                .address(request.getAddress())
                .concertHallUrl(request.getConcertHallUrl())
                .build());
        return ApiResponse.success(null);
    }
}
