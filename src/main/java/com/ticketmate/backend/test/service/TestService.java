package com.ticketmate.backend.test.service;

import static com.ticketmate.backend.domain.member.domain.constant.MemberType.AGENT;
import static com.ticketmate.backend.domain.member.domain.constant.MemberType.CLIENT;
import static com.ticketmate.backend.domain.member.domain.constant.Role.ROLE_TEST;
import static com.ticketmate.backend.domain.member.domain.constant.Role.ROLE_TEST_ADMIN;
import static com.ticketmate.backend.global.util.common.CommonUtil.null2ZeroInt;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.test.dto.request.LoginRequest;
import com.ticketmate.backend.test.dto.response.LoginResponse;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationFormDetail;
import com.ticketmate.backend.domain.applicationform.domain.entity.HopeArea;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.applicationform.repository.ApplicationFormRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertDateRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepository;
import com.ticketmate.backend.domain.concert.repository.TicketOpenDateRepository;
import com.ticketmate.backend.domain.concerthall.repository.ConcertHallRepository;
import com.ticketmate.backend.domain.member.repository.MemberRepository;
import com.ticketmate.backend.domain.portfolio.repository.PortfolioRepository;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {

  // 배치 사이즈
  private static final int BATCH_SIZE = 500;
  private final MemberRepository memberRepository;
  private final Faker koFaker = new Faker(new Locale("ko", "KR"));
  private final MockMemberFactory mockMemberFactory;
  private final MockConcertFactory mockConcertFactory;
  private final MockPortfolioFactory mockPortfolioFactory;
  private final JwtUtil jwtUtil;
  @Qualifier("applicationTaskExecutor")
  private final TaskExecutor taskExecutor;
  private final ConcertRepository concertRepository;
  private final ConcertDateRepository concertDateRepository;
  private final TicketOpenDateRepository ticketOpenDateRepository;
  private final ConcertHallRepository concertHallRepository;
  private final ApplicationFormRepository applicationFormRepository;
  private final PortfolioRepository portfolioRepository;
  private final TransactionTemplate transactionTemplate;

    /*
    ======================================회원======================================
     */

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
  public LoginResponse testSocialLogin(LoginRequest request) {

    log.debug("테스트 계정 로그인을 집행합니다. 요청 소셜 플랫폼: {}", request.getSocialPlatform());

    Member member = memberRepository.findByUsername(request.getUsername())
        .orElseGet(() -> memberRepository.save(mockMemberFactory.generate(request)));
    CustomOAuth2User customOAuth2User = new CustomOAuth2User(member, null);
    String accessToken = jwtUtil.createAccessToken(customOAuth2User);

    log.debug("테스트 로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
    log.debug("테스트 accessToken = {}", accessToken);

    return LoginResponse.builder()
        .memberId(member.getMemberId())
        .memberType(member.getMemberType())
        .accessToken(accessToken)
        .build();
  }

  /**
   * 사용자로부터 원하는 개수를 입력받아 회원 Mock 데이터를 추가합니다
   * 해당 메서드는 멀티스레드를 사용하여 동작합니다
   */
  @Transactional
  public CompletableFuture<Void> generateMemberMockDataAsync(int count) {
    long startMs = System.currentTimeMillis();
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    List<Member> memberList = Collections.synchronizedList(new ArrayList<>());

    // 각 회원 생성을 별도 스레드에서 처리
    for (int i = 0; i < count; i++) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        try {
          Member member = mockMemberFactory.generate();
          memberList.add(member); // Collections.synchronizedList 사용으로 이미 스레드 세이프
        } catch (Exception e) {
          log.error("회원 Mock 데이터 생성 중 오류 발생: {}", e.getMessage());
          throw new CustomException(ErrorCode.GENERATE_MOCK_DATA_ERROR);
        }
      }, taskExecutor);
      futures.add(future);
    }

    // 모든 비동기 작업이 완료될 때까지 대기
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(v -> {
          try {
            // 트랜잭션 내에서 일괄 저장
            List<Member> savedMemberList = memberRepository.saveAll(memberList);
            long endMs = System.currentTimeMillis();
            log.debug("회원 Mock 데이터 {}개 생성 및 저장 완료: 소요시간: {}ms",
                savedMemberList.size(), endMs - startMs);
            return null;
          } catch (Exception e) {
            log.error("회원 Mock 데이터 저장 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
          }
        });
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
  public CompletableFuture<Void> createConcertHallMockData(Integer count) {

    log.debug("공연장 Mock 데이터 저장을 시작합니다");
    long startMs = System.currentTimeMillis();
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
          throw new CustomException(ErrorCode.GENERATE_MOCK_DATA_ERROR);
        }
      }, taskExecutor);
      futures.add(future);
    }

    // 모든 비동기 작업이 완료될 때까지 대기
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(v -> {
          try {
            // 공연장 Mock데이터 저장
            concertHallRepository.saveAll(concertHalls);
            long endMs = System.currentTimeMillis();
            log.debug("공연장 Mock 데이터 저장 완료, 저장된 개수: {}", concertHalls.size());
            log.debug("공연장 Mock 데이터 멀티스레드 저장 소요 시간: {}ms", endMs - startMs);
            return null;
          } catch (Exception e) {
            log.error("공연장 데이터 저장 중 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
          }
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
   * 배치 단위로 멀티스레드를 사용하여 동작합니다
   */
  @Transactional
  public CompletableFuture<Void> generateConcertMockDataAsync(Integer count) {

    log.debug("공연 Mock 데이터 저장을 시작합니다.");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    int total = null2ZeroInt(count) == 0 ? 30 : count;

    // 데이터베이스에서 공연장 목록 조회
    List<ConcertHall> concertHallList = concertHallRepository.findAll();
    if (CommonUtil.nullOrEmpty(concertHallList)) {
      log.error("저장된 공연장이 없습니다. 공연장 Mock 데이터를 먼저 생성하세요");
      throw new CustomException(ErrorCode.CONCERT_HALL_NOT_FOUND);
    }

    // 배치 사이즈 계산
    int totalBatches = (total + BATCH_SIZE - 1) / BATCH_SIZE;

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    // 멀티스레드 처리
    for (int batch = 0; batch < totalBatches; batch++) {
      int startIndex = batch * BATCH_SIZE;
      int size = Math.min(BATCH_SIZE, total - startIndex);

      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        try {

          // Mock 데이터 생성
          List<Concert> concertList = new ArrayList<>();
          List<List<ConcertDate>> concertDateLists = new ArrayList<>();
          List<List<TicketOpenDate>> ticketOpenDateLists = new ArrayList<>();

          for (int i = 0; i < size; i++) {
            Concert concert = mockConcertFactory.generate(concertHallList);
            concertList.add(concert);
            concertDateLists.add(mockConcertFactory.generateConcertDateList(concert));
            ticketOpenDateLists.add(mockConcertFactory.generalTicketOpenDateList(concert));
          }

          // 배치 단위 트랜잭션 저장
          transactionTemplate.execute(status -> {
            concertRepository.saveAll(concertList);
            concertDateLists.forEach(list -> {
              if (!CommonUtil.nullOrEmpty(list)) {
                concertDateRepository.saveAll(list);
              }
            });
            ticketOpenDateLists.forEach(list -> {
              if (!CommonUtil.nullOrEmpty(list)) {
                ticketOpenDateRepository.saveAll(list);
              }
            });
            return null;
          });
        } catch (Exception e) {
          log.error("공연 Mock 데이터 청크 저장 실패: {}", e.getMessage());
          throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
        }
      }, taskExecutor);
      futures.add(future);
    }

    // 모든 비동기 작업이 완료될 때까지 대기
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .handle((res, ex) -> {
          if (ex != null) {
            log.error("공연 Mock 데이터 전체 저장 중 오류: {}", ex.getMessage(), ex);
            throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
          }
          stopWatch.stop();
          log.debug("공연 Mock 데이터 저장 완료: 총 {}건, 소요시간 {}ms", total, stopWatch.getTotalTimeMillis());
          return null;
        });
  }

    /*
    ======================================신청서======================================
     */

  /**
   * 사용자로부터 원하는 개수를 입력받아 신청서 Mock 데이터를 추가합니다
   * 해당 메서드는 멀티 스레드를 사용하여 동작합니다
   */
  @Transactional
  public CompletableFuture<Void> createApplicationMockData(Integer count) {

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
          applicationForms.add(applicationForm);
        } catch (Exception e) {
          log.error("신청서 Mock 데이터 멀티스레드 저장 중 오류 발생: {}", e.getMessage());
          throw new CustomException(ErrorCode.GENERATE_MOCK_DATA_ERROR);
        }
      }, taskExecutor);
      futures.add(future);
    }

    // 모든 비동기 작업이 완료될 때까지 대기
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(v -> {
          try {
            applicationFormRepository.saveAll(applicationForms);
            long endMs = System.currentTimeMillis();
            log.debug("신청서 Mock 데이터 저장 완료, 저장된 개수: {}", applicationForms.size());
            log.debug("신청서 Mock 데이터 멀티스레드 저장 소요 시간: {}ms", endMs - startMs);
            return null;
          } catch (Exception e) {
            log.error("신청서 Mock 데이터 저장 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
          }
        });
  }

  /**
   * 신청서 단일 Mock 데이터를 생성합니다 (저장 X)
   *
   * @param agentList   DB에 저장된 대리인 리스트
   * @param clientList  DB에 저장된 의뢰인 리스트
   * @param concertList DB에 저장된 콘서트 리스트
   * @return 생성된 신청서 Mock데이터
   */
  private ApplicationForm createApplicationMockData(List<Member> agentList, List<Member> clientList, List<Concert> concertList) {

    // 의뢰인 (DB에서 랜덤 선택)
    Member client = clientList.get(koFaker.random().nextInt(clientList.size()));

    // 대리인 (DB에서 랜덤 선택)
    Member agent = agentList.get(koFaker.random().nextInt(agentList.size()));

    // 콘서트 (DB에서 랜덤 선택)
    Concert concert = concertList.get(koFaker.random().nextInt(concertList.size()));

    // 티켓 오픈일 (TicketOpenDate) 생성
    List<TicketOpenDate> ticketOpenDateList = ticketOpenDateRepository.findAllByConcertConcertId(concert.getConcertId());
    TicketOpenDate ticketOpenDate = ticketOpenDateList.get(koFaker.random().nextInt(ticketOpenDateList.size()));

    // 신청서 상태 (랜덤)
    ApplicationFormStatus applicationFormStatus = ApplicationFormStatus
        .values()[koFaker.random().nextInt(ApplicationFormStatus.values().length)];

    // 선예매/일반예매 (랜덤)
    TicketOpenType ticketOpenType = ticketOpenDate.getTicketOpenType();

    ApplicationForm applicationForm = ApplicationForm.builder()
        .client(client)
        .agent(agent)
        .concert(concert)
        .ticketOpenDate(ticketOpenDate)
        .applicationFormDetailList(new ArrayList<>())
        .applicationFormStatus(applicationFormStatus)
        .ticketOpenType(ticketOpenType)
        .build();

    // 신청서 세부사항 추가
    // 양방향 연관관계
    createApplicationFormDetailList(concert, ticketOpenType)
        .forEach(applicationForm::addApplicationFormDetail);

    return applicationForm;
  }

  /**
   * 신청서 세부사항 Mock 데이터를 생성합니다
   */
  private List<ApplicationFormDetail> createApplicationFormDetailList(Concert concert, TicketOpenType ticketOpenType) {
    List<ConcertDate> concertDateList = concertDateRepository.findAllByConcertConcertId(concert.getConcertId());
    if (CommonUtil.nullOrEmpty(concertDateList)) {
      log.error("신청서 세부사항 Mock 데이터 생성 중 공연: {}에 해당하는 공연 날짜가 존재하지 않습니다.", concert.getConcertName());
      throw new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND);
    }
    int size = koFaker.random().nextInt(1, concertDateList.size() + 1);
    List<ApplicationFormDetail> applicationFormDetailList = new ArrayList<>();

    // 선예매/일반예매 예매일 조회
    TicketOpenDate ticketOpenDate = ticketOpenDateRepository
        .findByConcertConcertIdAndTicketOpenType(concert.getConcertId(), ticketOpenType)
        .orElseThrow(() -> {
          log.error("신청서 세부사항 Mock 데이터 생성 중 공연: {}, 예매타입: {}에 해당하는 TicketOpenDate 정보가 존재하지 않습니다.",
              concert.getConcertName(), ticketOpenType.getDescription());
          return new CustomException(ErrorCode.TICKET_OPEN_DATE_NOT_FOUND);
        });

    // 공연일자를 랜덤하게 섞기
    Collections.shuffle(concertDateList);

    for (int i = 0; i < size; i++) {
      // 세부 요청별 요청 매수 (1 ~ Max장)
      int requestCount = koFaker.random().nextInt(1, ticketOpenDate.getRequestMaxCount() + 1);

      // ApplicationFormDetail 생성
      ApplicationFormDetail applicationFormDetail = ApplicationFormDetail.builder()
          .concertDate(concertDateList.get(i))
          .requestCount(requestCount)
          .requirement(koFaker.lorem().sentence(5, 10))
          .hopeAreaList(new ArrayList<>())
          .build();

      // 희망구역 추가 (양방향 관계 설정)
      List<HopeArea> hopeAreaList = createHopeAreaList();
      if (!CommonUtil.nullOrEmpty(hopeAreaList)) {
        hopeAreaList.forEach(applicationFormDetail::addHopeArea);
      }
      applicationFormDetailList.add(applicationFormDetail);
    }

    return applicationFormDetailList;
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

    /*
    ======================================포트폴리오======================================
     */

  /**
   * 사용자로부터 원하는 개수를 입력받아 포트폴리오 Mock 데이터를 추가합니다
   * 해당 메서드는 멀티스레드를 사용하여 동작합니다
   */
  @Transactional
  public CompletableFuture<Void> generateMockPortfoliosAsync(int count) {
    long startMs = System.currentTimeMillis();
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    List<Portfolio> portfolioList = Collections.synchronizedList(new ArrayList<>());

    // 각 포트폴리오 생성을 별도 스레드에서 처리
    for (int i = 0; i < count; i++) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        try {
          Portfolio portfolio = mockPortfolioFactory.generate();
          synchronized (this) {
            portfolioList.add(portfolio);
          }
        } catch (Exception e) {
          log.error("포트폴리오 Mock 데이터 생성 중 오류 발생: {}", e.getMessage());
          throw new CustomException(ErrorCode.GENERATE_MOCK_DATA_ERROR);
        }
      }, taskExecutor);
      futures.add(future);
    }

    // 모든 비동기 작업이 완료될 때까지 대기
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(v -> {
          try {
            // 트랜잭션 내에서 일괄 저장
            List<Portfolio> savedPortfolioList = portfolioRepository.saveAll(portfolioList);
            long endMs = System.currentTimeMillis();
            log.debug("포트폴리오 Mock 데이터 {}개 생성 및 저장 완료: 소요시간: {}ms",
                savedPortfolioList.size(), endMs - startMs);
            return null;
          } catch (Exception e) {
            log.error("포트폴리오 Mock 데이터 저장 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
          }
        });
  }
}
