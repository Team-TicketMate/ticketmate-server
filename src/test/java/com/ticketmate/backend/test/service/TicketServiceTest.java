package com.ticketmate.backend.test.service;

import com.ticketmate.backend.test.object.Ticket;
import com.ticketmate.backend.test.repository.TicketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TicketServiceTest {

    @Autowired
    TicketService ticketService;

    @Autowired
    TicketRepository ticketRepository;

    // 동시성 테스트용 변수
    private static final int CONCURRENT_COUNT = 100;

    // 테스트마다 새 티켓을 1000장으로 초기화
    private Long TICKET_ID;

    @BeforeEach
    public void beforeEach() {
        // 기존 데이터 정리
        ticketRepository.deleteAll();

        System.out.println("1000개의 티켓 생성");
        Ticket ticket = new Ticket(1000L);
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        TICKET_ID = saved.getId();
    }
    @AfterEach
    public void afterEach() {
        ticketRepository.deleteAll();
    }

    // 공통 로직: 동시에 100번 요청 보내서 ticketService의 메서드를 호출
    private void concurrentTicketingTest(Consumer<Void> action) throws InterruptedException {
        // 초기 수량
        Long originQuantity = ticketRepository.findById(TICKET_ID)
                .orElseThrow()
                .getQuantity();

        // 동시 호출
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_COUNT);

        for (int i = 0; i < CONCURRENT_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    action.accept(null);  // 실제로 티켓 차감 로직 호출
                } finally {
                    latch.countDown();   // 스레드 종료 시 latch 카운트 감소
                }
            });
        }

        // 모든 스레드가 끝날 때까지 대기
        latch.await();
        executorService.shutdown();

        // 최종 수량 검증
        Ticket ticket = ticketRepository.findById(TICKET_ID).orElseThrow();
        // 기대값: originQuantity - 100
        assertEquals(originQuantity - CONCURRENT_COUNT, ticket.getQuantity());
    }
    @Test
    @DisplayName("동시에 100명의 티켓팅 : 동시성 이슈(락 미사용)")
    public void badTicketingTest() throws Exception {
        concurrentTicketingTest((_no) -> ticketService.ticketing(TICKET_ID, 1L));
    }

    @Test
    @DisplayName("동시에 100명의 티켓팅 : Redisson 분산락 사용")
    public void redissonTicketingTest() throws Exception {
        concurrentTicketingTest((_no) -> ticketService.redissonTicketing(TICKET_ID, 1L));
    }
}