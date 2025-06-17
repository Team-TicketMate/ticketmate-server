package com.ticketmate.backend.test.service;

import com.ticketmate.backend.test.object.Ticket;
import com.ticketmate.backend.test.repository.TicketRepository;
import com.ticketmate.backend.util.redisson.RedisLockManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

  /**
   * 분산락 테스팅을 위한 클래스입니다.
   * 그저 테스팅을 위한 클래스이기 때문에 구체적으로 설계하지는 않았습니다.
   */

  private final TicketRepository ticketRepository;
  private final RedisLockManager redisLockManager;

  // 동시성 제어 없음
  public void ticketing(UUID ticketId, Long amount) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new IllegalArgumentException("티켓이 존재하지 않습니다."));
    ticket.decrease(amount);
    ticketRepository.save(ticket);
  }

  // Redisson 기반 분산 락 사용
  public void redissonTicketing(UUID ticketId, Long amount) {
    String lockKey = "ticket:" + ticketId;

    // 대기시간을 너무 짧게하면 tryLock 내부에서 false가 반환되어 예외가 터져 해당 스레드 로직은 작업진행이 불가능합니다.
    redisLockManager.executeLock(lockKey, 60L, 5L, () -> {
      Ticket ticket = ticketRepository.findById(ticketId)
          .orElseThrow(() -> new IllegalArgumentException("티켓이 존재하지 않습니다."));
      ticket.decrease(amount);
      ticketRepository.save(ticket);
      return null; // 작업 결과가 없으므로 null 반환
    });
  }
}
