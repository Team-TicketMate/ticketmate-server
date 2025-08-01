package com.ticketmate.backend.admin.application.service;

import com.ticketmate.backend.admin.application.dto.request.TicketOpenDateRequest;
import com.ticketmate.backend.admin.application.mapper.ConcertAdminMapper;
import com.ticketmate.backend.admin.application.validator.TicketOpenDateValidator;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.repository.TicketOpenDateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketOpenDateAdminService {

  private final TicketOpenDateRepository ticketOpenDateRepository;
  private final ConcertAdminMapper concertAdminMapper;

  /**
   * 기존에 저장된 티켓 오픈일을 모두 삭제하고 새로운 티켓 오픈일을 저장
   */
  public void replaceAllTicketOpenDateList(Concert concert, List<TicketOpenDateRequest> requestList) {
    log.debug("기존 티켓 오픈일을 모두 삭제하고, 새로운 티켓 오픈일을 저장합니다");
    validateTicketOpenDateList(requestList);
    deleteAllTicketOpenDateByConcert(concert);
    saveTicketOpenDateList(requestList);
  }

  /**
   * TicketOpenDate 리스트 저장
   *
   * @param requestList 티켓 오픈일 DTO 리스트
   */
  public void saveTicketOpenDateList(List<TicketOpenDateRequest> requestList) {
    ticketOpenDateRepository.saveAll(concertAdminMapper.toTicketOpenDateList(requestList));
  }

  /**
   * TicketOpenDateRequest 리스트 검증
   */
  public void validateTicketOpenDateList(List<TicketOpenDateRequest> requestList) {
    TicketOpenDateValidator.of(requestList)
        .singlePreOpen()
        .singleGeneralOpen();
  }

  /**
   * 특정 공연에 저장된 티켓오픈일(TicketOpenDate)을 모두 삭제합니다
   */
  private void deleteAllTicketOpenDateByConcert(Concert concert) {
    ticketOpenDateRepository.deleteAllByConcertConcertId(concert.getConcertId());

  }
}
