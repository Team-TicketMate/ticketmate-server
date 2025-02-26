package com.ticketmate.backend.service.test;

import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concerthall.ConcertHallRepository;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.ticketmate.backend.util.common.CommonUtil.null2ZeroInt;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {

    private final MemberRepository memberRepository;
    private final Faker koFaker = new Faker(new Locale("ko", "KR"));
    private final Faker enFaker = new Faker(new Locale("en"));
    private final JwtUtil jwtUtil;
    @Qualifier("applicationTaskExecutor")
    private final TaskExecutor taskExecutor;
    private final ConcertRepository concertRepository;
    private final ConcertHallRepository concertHallRepository;

    /*
    ======================================회원======================================
     */

    /**
     * 개발자용 테스트 로그인 로직
     * DB에 테스트 유저를 만든 후, 해당 사용자의 엑세스 토큰을 발급합니다.
     *
     * @param request socialPlatform 네이버/카카오 소셜 로그인 플랫폼
     *                memberType 의로인/대리인
     *                accountStatus 활성화/삭제
     *                isFirstLogin 첫 로그인 여부
     */
    @Transactional
    public String testSocialLogin(LoginRequest request) {
        LocalDate birth = koFaker.timeAndDate().birthday();
        log.debug("Faker 생성 birth: {}", birth);
        String birthYear = Integer.toString(birth.getYear()); // YYYY
        String birthDay = String.format("%02d%02d", birth.getMonthValue(), birth.getDayOfMonth()); // MMDD

        log.debug("테스트 계정 로그인을 집행합니다. 요청 소셜 플랫폼: {}", request.getSocialPlatform());
        Member testMember = Member.builder()
                .socialLoginId(UUID.randomUUID().toString())
                .username(enFaker.internet().emailAddress().replaceAll("[^a-zA-Z0-9@\\s]", ""))
                .nickname(enFaker.lorem().word())
                .name(koFaker.name().name().replaceAll(" ", ""))
                .socialPlatform(request.getSocialPlatform())
                .birthDay(birthYear)
                .birthYear(birthDay)
                .phone(koFaker.phoneNumber().cellPhone())
                .profileUrl(koFaker.internet().image())
                .gender(koFaker.options().option("male", "female"))
                .role(Role.ROLE_TEST)
                .memberType(request.getMemberType())
                .accountStatus(request.getAccountStatus())
                .isFirstLogin(request.getIsFirstLogin())
                .lastLoginTime(LocalDateTime.now())
                .build();
        memberRepository.save(testMember);

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(testMember, null);
        String accessToken = jwtUtil.createAccessToken(customOAuth2User);

        log.debug("테스트 로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
        log.debug("테스트 accessToken = {}", accessToken);

        return accessToken;
    }

    /**
     * 데이터베이스에 저장되어있는 테스트 유저를 모두 삭제합니다
     */
    @Transactional
    public void deleteTestMember() {
        log.debug("데이터베이스에 저장된 테스트 유저를 모두 삭제합니다.");
        memberRepository.deleteAllByRole(Role.ROLE_TEST);
    }

    /*
    ======================================공연장======================================
     */

    /**
     * 사용자로부터 원하는 개수를 입력받아 공연장 Mock 데이터를 추가합니다
     * 해당 메서드는 멀티스레드를 사용하여 동작합니다
     */
    @Transactional
    public void createConcertHallMockData(Integer count) {

        log.debug("공연장 Mock 데이터 저장을 시작합니다");
        count = null2ZeroInt(count) == 0 ? 30 : count; // 기본 30개 데이터 추가

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<ConcertHall> concertHalls = Collections.synchronizedList(new ArrayList<>());

        // 멀티스레드 처리
        for (int i = 0; i < count; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    ConcertHall concertHall = createConcertHallMockData();
                    synchronized (this) { // 중복 체크를 위한 동기화
                        if (!concertHallRepository.existsByConcertHallName(concertHall.getConcertHallName())) {
                            concertHalls.add(concertHall);
                        } else {
                            log.debug("중복된 공연장 이름 스킵: {}", concertHall.getConcertHallName());
                        }
                    }
                } catch (Exception e) {
                    log.error("공연장 데이터 멀티스레드 저장 중 오류: {}", e.getMessage());
                }
            }, taskExecutor);
            futures.add(future);
        }

        // 모든 비동기 작업이 완료될 때까지 대기
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    try {
                        // 공연장 Mock데이터 저장
                        concertHallRepository.saveAll(concertHalls);
                        log.debug("공연장 Mock 데이터 저장 완료, 저장된 개수: {}", concertHalls.size());
                    } catch (Exception e) {
                        log.error("공연장 데이터 저장 중 오류: {}", e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("공연장 Mock 데이터 생성 중 오류 발생: {}", throwable.getMessage());
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                });
    }

    /**
     * 공연장 단일 Mock 데이터를 생성 후 반환합니다.
     */
    private ConcertHall createConcertHallMockData() {

        String concertHallName = koFaker.music().instrument() + " " + koFaker.book().author() + koFaker.random().nextInt(1, 1001) + "공연장";
        String address = koFaker.address().fullAddress();
        City city = City.determineCityFromAddress(address);
        String url = "https://www." + koFaker.internet().domainName();

        return ConcertHall.builder()
                .concertHallName(concertHallName)
                .capacity(koFaker.number().numberBetween(10000, 50001) / 1000 * 1000)
                .address(address)
                .city(city)
                .concertHallUrl(url)
                .build();
    }

    /*
    ======================================공연======================================
     */

    /**
     * 사용자로부터 원하는 개수를 입력받아 공연 Mock 데이터를 추가합니다
     * 해당 메서드는 멀티스레드를 사용하여 동작합니다
     */
    @Transactional
    public void createConcertMockData(Integer count) {

        log.debug("공연 Mock 데이터 저장을 시작합니다.");
        count = null2ZeroInt(count) == 0 ? 30 : count;

        // 데이터베이스에서 공연장 목록 조회
        List<ConcertHall> concertHallList = concertHallRepository.findAll();
        if (concertHallList.isEmpty()) {
            log.error("저장된 공연장이 없습니다. 공연장 Mock 데이터를 먼저 생성하세요");
            throw new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<Concert> concertList = Collections.synchronizedList(new ArrayList<>());

        // 멀티스레드 처리
        for (int i = 0; i < count; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Concert concert = createConcertMockData(concertHallList);
                    synchronized (this) {
                        concertList.add(concert);
                    }
                } catch (Exception e) {
                    log.error("공연 데이터 멀티스레드 저장 중 오류 발생: {}", e.getMessage());
                }
            }, taskExecutor);
            futures.add(future);
        }

        // 모든 비동기 작업이 완료될 때까지 대기
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    try {
                        concertRepository.saveAll(concertList);
                        log.debug("공연 Mock 데이터 저장 완료, 저장된 개수: {}", concertList.size());
                    } catch (Exception e) {
                        log.error("공연 데이터 저장 중 오류 발생: {}", e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("공연 Mock 데이터 생성 중 오류 발생: {}", throwable.getMessage());
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                });
    }

    /**
     * 공연 단일 Mock 데이터를 생성 후 반환합니다.
     */
    private Concert createConcertMockData(List<ConcertHall> concertHallList) {

        // 1. 공연 이름
        String concertName = koFaker.music().genre() + " " + koFaker.team().name() + "공연";

        // 2. 공연장 (DB에서 랜덤 선택)
        ConcertHall concertHall = concertHallList.get(koFaker.random().nextInt(concertHallList.size()));

        // 3. 공연 카테고리 (랜덤)
        ConcertType concertType = ConcertType.values()[koFaker.random().nextInt(ConcertType.values().length)];

        // 4. 선구매 오픈일 (랜덤)
        LocalDateTime ticketPreOpenDate = koFaker.random().nextBoolean() ?
                LocalDateTime.now().plusDays(koFaker.number().numberBetween(1, 30)) : null;

        // 5. 티켓 오픈일 (선구매 오픈일 이후)
        LocalDateTime ticketOpenDate = ticketPreOpenDate != null ?
                ticketPreOpenDate.plusDays(koFaker.number().numberBetween(1, 15)) :
                LocalDateTime.now().plusDays(koFaker.number().numberBetween(1, 45));

        // 6. 공연 시간 (60 ~ 180분)
        int duration = koFaker.number().numberBetween(6, 19) * 10;

        // 7. 공연 회차 (1 ~ 3)
        int session = koFaker.number().numberBetween(1, 4);

        // 8. 썸네일 이미지 URL (랜덤)
        String concertThumbnailUrl = koFaker.internet().image();

        // 9. 예매처 (랜덤)
        TicketReservationSite ticketReservationSite = TicketReservationSite
                .values()[koFaker.random().nextInt(TicketReservationSite.values().length)];

        return Concert.builder()
                .concertName(concertName)
                .concertHall(concertHall)
                .concertType(concertType)
                .ticketPreOpenDate(ticketPreOpenDate)
                .ticketOpenDate(ticketOpenDate)
                .duration(duration)
                .session(session)
                .concertThumbnailUrl(concertThumbnailUrl)
                .ticketReservationSite(ticketReservationSite)
                .build();
    }
}
