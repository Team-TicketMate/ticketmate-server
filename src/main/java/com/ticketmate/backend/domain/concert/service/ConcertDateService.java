package com.ticketmate.backend.domain.concert.service;

import com.ticketmate.backend.domain.admin.dto.request.ConcertDateRequest;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.repository.ConcertDateRepository;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.validator.concert.ConcertDateValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertDateService {

  private final ConcertDateRepository concertDateRepository;
  private final EntityMapper entityMapper;

  /**
   * 기존에 저장된 공연날짜를 모두 삭제하고 새로운 공연날짜를 저장
   */
  public void replaceAllConcertDateList(Concert concert, List<ConcertDateRequest> requestList) {
    log.debug("기존 공연 날짜를 모두 삭제하고, 새로운 공연 날짜를 저장합니다");
    validateConcertDateList(requestList);
    deleteAllConcertDateByConcert(concert);
    saveConcertDateList(requestList);
  }

  /**
   * ConcertDate 리스트 저장
   *
   * @param requestList 공연 날짜 DTO 리스트
   */
  public void saveConcertDateList(List<ConcertDateRequest> requestList) {
    concertDateRepository.saveAll(entityMapper.toConcertDateList(requestList));
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
