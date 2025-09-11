package com.ticketmate.backend.admin.concert.application.service;

import com.ticketmate.backend.admin.concert.application.dto.request.ConcertDateRequest;
import com.ticketmate.backend.admin.concert.application.validator.ConcertDateValidator;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertDateRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertDateAdminService {

  private final ConcertDateRepository concertDateRepository;

  /**
   * 기존에 저장된 공연날짜를 모두 삭제하고 새로운 공연날짜를 저장
   */
  public void replaceAllConcertDateList(Concert concert, List<ConcertDateRequest> requestList) {
    log.debug("기존 공연 날짜를 모두 삭제하고, 새로운 공연 날짜를 저장합니다");
    validateConcertDateList(requestList);
    deleteAllConcertDateByConcert(concert);
    saveConcertDateList(concert, requestList);
  }

  /**
   * ConcertDate 리스트 저장
   *
   * @param requestList 공연 날짜 DTO 리스트
   */
  public void saveConcertDateList(Concert concert, List<ConcertDateRequest> requestList) {
    List<ConcertDate> concertDateList = requestList.stream()
        .map(request -> ConcertDate.of(concert, TimeUtil.toInstant(request.getPerformanceDate()), request.getSession()))
        .collect(Collectors.toList());
    concertDateRepository.saveAll(concertDateList);
  }

  /**
   * ConcertDateRequest 리스트 검증
   */
  public void validateConcertDateList(List<ConcertDateRequest> requestList) {
    ConcertDateValidator.of(requestList)
        .sessionStartsAtOne()
        .sessionContinuity()
        .dateSessionOrder();
  }

  /**
   * 특정 공연에 저장된 공연일자(ConcertDate)를 모두 삭제합니다
   */
  private void deleteAllConcertDateByConcert(Concert concert) {
    concertDateRepository.deleteAllByConcertConcertId(concert.getConcertId());
  }
}
