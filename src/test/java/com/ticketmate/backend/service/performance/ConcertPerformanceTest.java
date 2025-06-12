package com.ticketmate.backend.service.performance;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.repository.postgres.concert.*;
import com.ticketmate.backend.service.concert.ConcertService;
import com.ticketmate.backend.service.test.TestService;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
//@Transactional
public class ConcertPerformanceTest {

    @Autowired
    TestService testService;

    @Autowired
    ConcertRepository concertRepository;

    @Autowired
    ConcertRepositoryImpl concertRepositoryImpl;

    @Autowired
    ConcertRepositoryOptimizedImpl concertRepositoryOptimizedImpl;

    @Autowired
    ConcertDateRepository concertDateRepository;

    @Autowired
    TicketOpenDateRepository ticketOpenDateRepository;

    @Autowired
    ConcertService concertService;

    List<ResultEntity> resultEntities = new ArrayList<>();

    /**
     * 워밍업 코드 (JIT, Hibernate, 커넥션 풀, DB 캐시를 미리 웜업)
     * 해당 워밍업 코드가 없는경우 첫번째 실행만 오래걸리는 문제 발생
     */
    @BeforeAll
    void warmUp() {
        ConcertFilteredRequest warmUpRequest = ConcertFilteredRequest.builder()
                .pageNumber(0)
                .pageSize(1)
                .sortField("created_date")
                .sortDirection("DESC")
                .build();
        concertService.filteredConcert(warmUpRequest);
    }

//    /**
//     * 테스트를 진행하기 전 Concert의 PK를 사용하는 테이블 초기화 (ex.ApplicationForm)
//     */
//    @BeforeEach
//    void setUp() {
//        concertDateRepository.deleteAllInBatch();
//        ticketOpenDateRepository.deleteAllInBatch();
//        concertRepository.deleteAllInBatch();
//        createConcertInBatch(100);
//    }


    @Test
    void 공연_Mock_데이터_추가() {
        createConcertMockData(10);
    }

    /**
     * N번 테스트 실행
     */
    @RepeatedTest(10)
    void 기존_공연_필터링_조회_테스트(RepetitionInfo info) {

        ConcertFilteredRequest request = ConcertFilteredRequest.builder()
                .concertName("e")
                .ConcertType(ConcertType.CONCERT)
                .pageNumber(0)
                .pageSize(30)
                .sortField("created_date")
                .sortDirection("DESC")
                .build();
        StopWatch stopWatch = new StopWatch("공연 필터링 조회");

        stopWatch.start();
        Page<ConcertFilteredResponse> concertFilteredResponses = concertService.filteredConcert(request);
        stopWatch.stop();

        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        long totalElements = concertFilteredResponses.getTotalElements();
        long numberOfElements = concertFilteredResponses.getNumberOfElements();

        resultEntities.add(ResultEntity.builder()
                .taskTotalTImeMillis(totalTimeMillis)
                .currentRepetition(info.getCurrentRepetition())
                .totalRepetition(info.getTotalRepetitions())
                .totalElements(totalElements)
                .numberOfElements(numberOfElements)
                .build());

        log.info(">>> 공연 필터링 조회 시간: {}ms", totalTimeMillis);
        log.info("공연 데이터 총 개수: {}, 페이지네이션 조회 개수: {}", totalElements, numberOfElements);
        assertThat(concertFilteredResponses).isNotEmpty();
    }

    @AfterAll
    void afterAll() {
        // 각 테스트 별 로깅
        resultEntities.forEach(r -> {
            log.info("[반복 {}/{}] 테스트 시간: {}ms, 총 데이터 수: {}, 조회 데이터 수: {}",
                    r.getCurrentRepetition(),
                    r.getTotalRepetition(),
                    r.getTaskTotalTImeMillis(),
                    r.getTotalElements(),
                    r.getNumberOfElements());
        });

        // 평균 계산
        double averageTime = resultEntities.stream()
                .mapToLong(ResultEntity::getTaskTotalTImeMillis)
                .average()
                .orElse(0.0);

        // 평균 로깅
        log.info(">>> 전체 {}회 반복 평균 수행 시간: {}ms",
                resultEntities.size(),
                String.format("%.3f", averageTime));
    }

    private void createConcertMockData(int count) {
        testService.generateConcertMockDataAsync(count).join();
    }

    @Builder
    @Getter
    private static class ResultEntity {
        private Long taskTotalTImeMillis;
        private Integer currentRepetition;
        private Integer totalRepetition;
        private Long totalElements;
        private Long numberOfElements;
    }

    @Test
    void 공연_JOIN_쿼리_서브_쿼리_출력_데이터_비교() {

        Pageable pageable = PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "created_date"));
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("Left Join 쿼리");
        Page<ConcertFilteredResponse> leftJoinResponse = concertRepositoryImpl
                .filteredConcert(
                        "",
                        "",
                        null,
                        null,
                        pageable
                );
        stopWatch.stop();
        log.info("Left Join 쿼리 소요시간: {}ms", stopWatch.getTotalTimeMillis());

        stopWatch.start("서브 쿼리");
        Page<ConcertFilteredResponse> subqueryResponse = concertRepositoryOptimizedImpl
                .filteredConcert(
                        "",
                        "",
                        null,
                        null,
                        pageable
                );
        stopWatch.stop();
        log.info("서브쿼리 소요시간: {}ms", stopWatch.getTotalTimeMillis());

        assertThat(leftJoinResponse.getContent())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(subqueryResponse.getContent());
        assertThat(leftJoinResponse.getTotalElements())
                .isEqualTo(subqueryResponse.getTotalElements());
    }
}
