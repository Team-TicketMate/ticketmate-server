package com.ticketmate.backend.service;

import com.ticketmate.backend.object.dto.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.ConcertInfoRequest;
import com.ticketmate.backend.object.postgres.Concert;
import com.ticketmate.backend.object.postgres.ConcertHall;
import com.ticketmate.backend.repository.postgres.ConcertHallRepository;
import com.ticketmate.backend.repository.postgres.ConcertRepository;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertHallRepository concertHallRepository;
    private final FileService fileService;

    /**
     * 콘서트 정보 저장
     *
     * @param request concertName 공연 제목
     *                concertHallName 공연장 이름
     *                concertType 공연 카테고리
     *                ticketPreOpenDate 선구매 오픈일
     *                ticketOpenDate 티켓 구매 오픈일
     *                duration 공연 시간 (분)
     *                seatPrices 좌석 가격
     *                session 공연 회차
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
     *
     * @param request
     * @return
     */
    @Transactional(readOnly = true)
    public Page<ConcertFilteredResponse> filteredConcert(ConcertFilteredRequest request) {
        return null;
    }
}
