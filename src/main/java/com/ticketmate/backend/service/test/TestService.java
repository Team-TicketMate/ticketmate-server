package com.ticketmate.backend.service.test;

import com.ticketmate.backend.object.constants.ApplicationStatus;
import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.object.postgres.application.HopeArea;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.repository.postgres.application.ApplicationFormRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertDateRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concert.TicketOpenDateRepository;
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

import static com.ticketmate.backend.object.constants.MemberType.AGENT;
import static com.ticketmate.backend.object.constants.MemberType.CLIENT;
import static com.ticketmate.backend.object.constants.Role.ROLE_TEST;
import static com.ticketmate.backend.object.constants.Role.ROLE_TEST_ADMIN;
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
    private final ConcertDateRepository concertDateRepository;
    private final TicketOpenDateRepository ticketOpenDateRepository;
    private final ConcertHallRepository concertHallRepository;
    private final ApplicationFormRepository applicationFormRepository;

    /*
    ======================================회원======================================
     */

    /**
     * 개발자용 회원 Mock 데이터 생성
     *
     * @param request socialPlatform 네이버/카카오 소셜 로그인 플랫폼
     *                memberType 의로인/대리인
     *                accountStatus 활성화/삭제
     *                isFirstLogin 첫 로그인 여부
     */
    @Transactional
    public Member createMockMember(LoginRequest request) {

        log.debug("테스트 Mock 회원을 생성합니다");
        LocalDate birth = koFaker.timeAndDate().birthday();
        String birthYear = Integer.toString(birth.getYear()); // YYYY
        String birthDay = String.format("%02d%02d", birth.getMonthValue(), birth.getDayOfMonth()); // MMDD

        // 테스트 회원 ROLE 검증
        if (request.getRole() != ROLE_TEST && request.getRole() != ROLE_TEST_ADMIN) {
            log.error("테스트 로그인은 ROLE_TEST, ROLE_TEST_ADMIN 권한만 생성 가능합니다. 요청된 ROLE: {}", request.getRole());
            throw new CustomException(ErrorCode.INVALID_MEMBER_ROLE_REQUEST);
        }

        return Member.builder()
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
                .role(request.getRole())
                .memberType(request.getMemberType())
                .accountStatus(request.getAccountStatus())
                .isFirstLogin(request.getIsFirstLogin())
                .lastLoginTime(LocalDateTime.now())
                .build();
    }

    /**
     * 개발자용 테스트 로그인 로직
     * DB에 테스트 유저를 만든 후, 해당 사용자의 엑세스 토큰을 발급합니다.
     *
     * @param request role 권한 (ROLE_TEST / ROLE_TEST_ADMIN)
     *                socialPlatform 네이버/카카오 소셜 로그인 플랫폼
     *                memberType 의로인/대리인
     *                accountStatus 활성화/삭제
     *                isFirstLogin 첫 로그인 여부
     */
    @Transactional
    public String testSocialLogin(LoginRequest request) {

        log.debug("테스트 계정 로그인을 집행합니다. 요청 소셜 플랫폼: {}", request.getSocialPlatform());

        Member member = memberRepository.save(createMockMember(request));
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(member, null);
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
        memberRepository.deleteAllByRole(ROLE_TEST);
        memberRepository.deleteAllByRole(ROLE_TEST_ADMIN);
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
     * 공연장 단일 Mock 데이터를 생성 후 반환합니다. (저장 X)
     */
    @Transactional
    public ConcertHall createConcertHallMockData() {

        String concertHallName = koFaker.music().instrument() + " " + koFaker.book().author() + koFaker.random().nextInt(1, 1001) + "공연장";
        String address = koFaker.address().fullAddress();
        City city = City.fromAddress(address);
        String url = "https://www." + koFaker.internet().domainName();

        return ConcertHall.builder()
                .concertHallName(concertHallName)
                .address(address)
                .city(city)
                .webSiteUrl(url)
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
                    // 공연과 관련된 객체 생성
                    Concert concert = createConcertMockData(concertHallList);
                    List<ConcertDate> concertDateList = createConcertDateList(concert);
                    List<TicketOpenDate> ticketOpenDateList = createTicketOpenDateList(concert);

                    synchronized (this) {
                        // Concert 저장
                        concertRepository.save(concert);
                        // ConcertDate 저장
                        if (!concertDateList.isEmpty()) {
                            concertDateRepository.saveAll(concertDateList);
                        }
                        // TicketOpenDate 저장
                        if (!ticketOpenDateList.isEmpty()) {
                            ticketOpenDateRepository.saveAll(ticketOpenDateList);
                        }
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
                        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("공연 Mock 데이터 생성 중 오류 발생: {}", throwable.getMessage());
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                });
    }

    /**
     * 공연 단일 Mock 데이터를 생성 후 반환합니다. (저장 X)
     */
    private Concert createConcertMockData(List<ConcertHall> concertHallList) {

        // 1. 공연 이름
        String concertName = koFaker.music().genre() + " " + koFaker.team().name() + "공연";

        // 2. 공연장 (DB에서 랜덤 선택)
        ConcertHall concertHall = concertHallList.get(koFaker.random().nextInt(concertHallList.size()));

        // 3. 공연 카테고리 (랜덤)
        ConcertType concertType = ConcertType.values()[koFaker.random().nextInt(ConcertType.values().length)];

        // 4. 썸네일 이미지 URL (랜덤)
        String concertThumbnailUrl = koFaker.internet().image();

        // 5. 좌석 배치도 이미지 URL (랜덤)
        String seatingChartUrl = koFaker.internet().image();

        // 6. 예매처 (랜덤)
        TicketReservationSite ticketReservationSite = TicketReservationSite
                .values()[koFaker.random().nextInt(TicketReservationSite.values().length)];

        return Concert.builder()
                .concertName(concertName)
                .concertHall(concertHall)
                .concertType(concertType)
                .concertThumbnailUrl(concertThumbnailUrl)
                .seatingChartUrl(seatingChartUrl)
                .ticketReservationSite(ticketReservationSite)
                .build();
    }

    /**
     * 공연 날짜 리스트를 생성합니다 (1~5개 랜덤)
     */
    private List<ConcertDate> createConcertDateList(Concert concert) {

        int size = koFaker.random().nextInt(1, 6);
        List<ConcertDate> concertDateList = new ArrayList<>();

        // 기준 날짜 (현재로부터 60~90 일 이후)
        LocalDateTime baseDate = LocalDateTime.now().plusDays(koFaker.number().numberBetween(60, 91));

        // 1. 공연 일자
        for (int i = 0; i < size; i++) {
            // 공연 날짜 (기준 날짜로 부터 하루 단위로 증가)
            LocalDateTime concertDate = baseDate.plusDays(i);

            concertDateList.add(ConcertDate.builder()
                    .concert(concert)
                    .performanceDate(concertDate)
                    .session(i + 1)
                    .build()
            );
        }
        return concertDateList;
    }

    /**
     * 티켓 오픈일 리스트를 생성합니다
     */
    private List<TicketOpenDate> createTicketOpenDateList(Concert concert) {
        List<TicketOpenDate> ticketOpenDateList = new ArrayList<>();

        // 기준 날짜 (현재로부터 10~30 일 이후)
        LocalDateTime baseDate = LocalDateTime.now().plusDays(koFaker.number().numberBetween(10, 31));

        // 1. 선예매 오픈일 (50% 확률로 생성)
        if (koFaker.random().nextBoolean()) {
            LocalDateTime preOpenDate = baseDate.plusDays(koFaker.number().numberBetween(0, 5));
            TicketOpenDate preOpen = TicketOpenDate.builder()
                    .concert(concert)
                    .openDate(preOpenDate)
                    .requestMaxCount(koFaker.number().numberBetween(1, 6))
                    .isBankTransfer(koFaker.random().nextBoolean())
                    .isPreOpen(true)
                    .build();
            ticketOpenDateList.add(preOpen);
        }

        // 2. 일반 예매 오픈일 (필수)
        LocalDateTime generalOpenDate = baseDate.plusDays(koFaker.number().numberBetween(5, 10));
        TicketOpenDate generalOpen = TicketOpenDate.builder()
                .concert(concert)
                .openDate(generalOpenDate)
                .requestMaxCount(koFaker.number().numberBetween(1, 6))
                .isBankTransfer(koFaker.random().nextBoolean())
                .isPreOpen(false)
                .build();
        ticketOpenDateList.add(generalOpen);

        return ticketOpenDateList;
    }

    /*
    ======================================신청서======================================
     */

    /**
     * 사용자로부터 원하는 개수를 입력받아 신청서 Mock 데이터를 추가합니다
     * 해당 메서드는 멀티 스레드를 사용하여 동작합니다
     */
    @Transactional
    public void createApplicationMockData(Integer count) {

        log.debug("신청서 Mock 데이터 저장을 시작합니다");
        long startMs = System.currentTimeMillis();
        count = null2ZeroInt(count) == 0 ? 30 : count;

        // 데이터베이스에서 대리인 목록 조회
        List<Member> agentList = memberRepository.findAllByMemberType(AGENT)
                .orElseThrow(() -> {
                    log.error("데이터베이스에 저장 된 대리인이 없습니다.");
                    return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
                });

        // 데이터베이스에서 의뢰인 목록 조회
        List<Member> clientList = memberRepository.findAllByMemberType(CLIENT)
                .orElseThrow(() -> {
                    log.error("데이터베이스에 저장 된 의뢰인이 없습니다.");
                    return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
                });

        // 데이터베이스에서 콘서트 목록 조회
        List<Concert> concertList = concertRepository.findAll();
        if (concertList.isEmpty()) {
            log.error("저장된 공연이 없습니다. 공연 Mock 데이터를 먼저 생성하세요.");
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<ApplicationForm> applicationForms = Collections.synchronizedList(new ArrayList<>());

        // 멀티스레드 처리
        for (int i = 0; i < count; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    ApplicationForm applicationForm = createApplicationMockData(agentList, clientList, concertList);
                    synchronized (this) {
                        applicationForms.add(applicationForm);
                    }
                } catch (Exception e) {
                    log.error("신청서 Mock 데이터 멀티스레드 저장 중 오류 발생: {}", e.getMessage());
                }
            }, taskExecutor);
            futures.add(future);
        }

        // 모든 비동기 작업이 완료될 때까지 대기
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    try {
                        applicationFormRepository.saveAll(applicationForms);
                        long endMs = System.currentTimeMillis();
                        log.debug("신청서 Mock 데이터 저장 완료, 저장된 개수: {}", applicationForms.size());
                        log.debug("신청서 Mock 데이터 멀티스레드 저장 소요 시간: {}ms", endMs - startMs);
                    } catch (Exception e) {
                        log.error("신청서 Mock 데이터 저장 중 오류 발생: {}", e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("신청서 Mock 데이터 생성 중 오류 발생: {}", throwable.getMessage());
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                });
    }

    /**
     * 신청서 단일 Mock 데이터를 생성합니다 (저장 X)
     *
     * @param agentList DB에 저장된 대리인 리스트
     * @param clientList DB에 저장된 의뢰인 리스트
     * @param concertList DB에 저장된 콘서트 리스트
     * @return 생성된 신청서 Mock데이터
     */
    private ApplicationForm createApplicationMockData(
            List<Member> agentList, List<Member> clientList, List<Concert> concertList) {

        // 1. 의뢰인 (DB에서 랜덤 선택)
        Member client = clientList.get(koFaker.random().nextInt(clientList.size()));

        // 2. 대리인 (DB에서 랜덤 선택)
        Member agent = agentList.get(koFaker.random().nextInt(agentList.size()));

        // 3. 콘서트 (DB에서 랜덤 선택)
        Concert concert = concertList.get(koFaker.random().nextInt(concertList.size()));

        // 4. 공연일자 (ConcertDate) 생성
        List<ConcertDate> concertDateList = concertDateRepository.findAllByConcert_ConcertId(concert.getConcertId());
        ConcertDate concertDate = concertDateList.get(koFaker.random().nextInt(concertDateList.size()));

        // 5. 티켓 오픈일 (TicketOpenDate) 생성
        List<TicketOpenDate> ticketOpenDateList = ticketOpenDateRepository.findAllByConcert_ConcertId(concert.getConcertId());
        TicketOpenDate ticketOpenDate = ticketOpenDateList.get(koFaker.random().nextInt(ticketOpenDateList.size()));

        // 6. 희망구역 리스트 (0 ~ 10개 랜덤)
        List<HopeArea> hopeAreaList = createHopeAreaList();

        // 7. 신청서 상태 (랜덤)
        ApplicationStatus applicationStatus = ApplicationStatus
                .values()[koFaker.random().nextInt(ApplicationStatus.values().length)];

        ApplicationForm applicationForm = ApplicationForm.builder()
                .client(client)
                .agent(agent)
                .concert(concert)
                .concertDate(concertDate)
                .requestCount(koFaker.number().numberBetween(1, ticketOpenDate.getRequestMaxCount() + 1))
                .hopeAreaList(new ArrayList<>())
                .requestDetails(koFaker.lorem().paragraph(2))
                .applicationStatus(applicationStatus)
                .build();

        hopeAreaList.forEach(applicationForm::addHopeArea);

        return applicationForm;
    }

    /**
     * 희망구역 리스트를 생성합니다 (0개 ~ 10개 랜덤)
     */
    private List<HopeArea> createHopeAreaList() {
        int size = koFaker.random().nextInt(11);
        List<HopeArea> hopeAreaList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            HopeArea hopeArea = HopeArea.builder()
                    .priority(i + 1)
                    .location(createRandomLocation())
                    .price(koFaker.number().numberBetween(1, 21) * 10000L)
                    .build();
            hopeAreaList.add(hopeArea);
        }
        return hopeAreaList;
    }

    /**
     * A13, E9, K30 과 같은 좌석번호를 랜덤하게 생성합니다
     * A~Z 알파벳, 1~30 정수 결합
     */
    private String createRandomLocation() {
        // A~Z 알파벳 랜덤 생성
        char randomLetter = (char) ('A' + koFaker.number().numberBetween(0, 26));
        // 1~30 랜덤 숫자 생성
        int randomNumber = koFaker.number().numberBetween(1, 31);
        // 문자열 결합
        return randomLetter + String.valueOf(randomNumber);
    }
}
