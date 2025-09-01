package com.ticketmate.backend.mock.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.null2ZeroInt;
import static com.ticketmate.backend.member.core.constant.MemberType.AGENT;
import static com.ticketmate.backend.member.core.constant.MemberType.CLIENT;
import static com.ticketmate.backend.member.core.constant.Role.ROLE_TEST;
import static com.ticketmate.backend.member.core.constant.Role.ROLE_TEST_ADMIN;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason;
import com.ticketmate.backend.applicationform.infrastructure.repository.ApplicationFormRepository;
import com.ticketmate.backend.applicationform.infrastructure.repository.RejectionReasonRepository;
import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.chat.infrastructure.repository.ChatRoomRepository;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertAgentAvailability;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertAgentAvailabilityRepository;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertDateRepository;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertRepository;
import com.ticketmate.backend.concert.infrastructure.repository.TicketOpenDateRepository;
import com.ticketmate.backend.concerthall.core.constant.City;
import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import com.ticketmate.backend.concerthall.infrastructure.repository.ConcertHallRepository;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.AgentPerformanceSummaryRepository;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import com.ticketmate.backend.mock.application.dto.request.MockLoginRequest;
import com.ticketmate.backend.mock.application.dto.response.MockChatRoomResponse;
import com.ticketmate.backend.mock.application.dto.response.MockLoginResponse;
import com.ticketmate.backend.mock.infrastructure.config.CachedConfig;
import com.ticketmate.backend.portfolio.application.service.PortfolioService;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.repository.PortfolioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
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
public class MockService {

  // 배치 사이즈
  private static final int BATCH_SIZE = 500;
  private final MemberRepository memberRepository;
  private final Faker koFaker = new Faker(new Locale("ko", "KR"));
  private final MockMemberFactory mockMemberFactory;
  private final MockConcertFactory mockConcertFactory;
  private final MockPortfolioFactory mockPortfolioFactory;
  private final MockApplicationFormFactory mockApplicationFormFactory;
  private final TokenProvider tokenProvider;
  @Qualifier("applicationTaskExecutor")
  private final TaskExecutor taskExecutor;
  private final ConcertRepository concertRepository;
  private final ConcertDateRepository concertDateRepository;
  private final TicketOpenDateRepository ticketOpenDateRepository;
  private final ConcertHallRepository concertHallRepository;
  private final ApplicationFormRepository applicationFormRepository;
  private final PortfolioRepository portfolioRepository;
  private final TransactionTemplate transactionTemplate;
  private final AgentPerformanceSummaryRepository agentPerformanceSummaryRepository;
  private final ConcertAgentAvailabilityRepository concertAgentAvailabilityRepository;
  private final PortfolioService portfolioService;
  private final RejectionReasonRepository rejectionReasonRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final MockChatRoomFactory mockChatRoomFactory;
  private static final String DEV_AGENT_USERNAME  = "test-chat-agent@ticketmate.com";
  private static final String DEV_CLIENT_USERNAME = "test-chat-client@ticketmate.com";
  private final AtomicReference<CachedConfig> cachedConfigAtomicReference = new AtomicReference<>();
  private final Clock clock;

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
  public MockLoginResponse testSocialLogin(MockLoginRequest request) {

    log.debug("테스트 계정 로그인을 집행합니다. 요청 소셜 플랫폼: {}", request.getSocialPlatform());

    Member member = memberRepository.findByUsername(request.getUsername())
        .orElseGet(() -> memberRepository.saveAndFlush(mockMemberFactory.generate(request)));
    if (request.getMemberType().equals(AGENT)) {
      Portfolio testPortfolio = mockPortfolioFactory.generate(member);
      portfolioRepository.save(testPortfolio);
      portfolioService.promoteToAgent(testPortfolio);
      log.debug("테스트 유저 {}를 대리인으로 승격 처리했습니다.", member.getMemberId());
    }

    String accessToken = tokenProvider.createAccessToken(member.getMemberId().toString(), member.getUsername(), member.getRole().name());

    log.debug("테스트 로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
    log.debug("테스트 accessToken = {}", accessToken);

    return MockLoginResponse.builder()
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

    List<Concert> concertList = concertRepository.findAll();
    if (concertList.isEmpty()) {
      log.error("저장된 공연이 없습니다. 공연 Mock 데이터를 먼저 생성하세요.");
      throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    List<CompletableFuture<Void>> futures = new ArrayList<>();
    List<Member> memberList = Collections.synchronizedList(new ArrayList<>());
    List<AgentPerformanceSummary> summaryList = Collections.synchronizedList(new ArrayList<>());
    List<ConcertAgentAvailability> availabilityList = Collections.synchronizedList(new ArrayList<>());

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
        .thenApply(v -> transactionTemplate.execute(status -> {
          try {
            // 트랜잭션 내에서 일괄 저장
            List<Member> savedMemberList = memberRepository.saveAll(memberList);

            for (Member savedMember : savedMemberList) {
              if (savedMember.getMemberType() == AGENT) {
                // AgentPerformanceSummary 생성 및 추가
                summaryList.add(mockMemberFactory.generatePerformanceSummary(savedMember));

                // ConcertAgentAvailability 생성 및 추가
                Concert randomConcert = concertList.get(koFaker.random().nextInt(concertList.size()));
                availabilityList.add(mockMemberFactory.generateAvailability(randomConcert, savedMember));
              }
            }
            agentPerformanceSummaryRepository.saveAll(summaryList);
            concertAgentAvailabilityRepository.saveAll(availabilityList);

            long endMs = System.currentTimeMillis();
            log.debug("회원 Mock 데이터 {}개 생성 및 저장 완료: 소요시간: {}ms",
                savedMemberList.size(), endMs - startMs);
            return null;
          } catch (Exception e) {
            log.error("회원 Mock 데이터 저장 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
          }
        })).thenAccept(v -> {
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
   * 해당 메서드는 멀티 스레드와 배치 처리를 사용하여 동작합니다
   */
  @Transactional
  public CompletableFuture<Void> generateApplicationFormMockDataAsync(Integer count) {

    log.debug("신청서 Mock 데이터 저장을 시작합니다");
    StopWatch stopwatch = new StopWatch();
    stopwatch.start();

    int total = null2ZeroInt(count) == 0 ? 30 : count;

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

    // 배치 사이즈 계산
    int totalBatches = (total + BATCH_SIZE - 1) / BATCH_SIZE;
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    // 배치 단위로 멀티스레드 처리
    for (int batch = 0; batch < totalBatches; batch++) {
      int startIndex = batch * BATCH_SIZE;
      int size = Math.min(BATCH_SIZE, total - startIndex);

      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        List<ApplicationForm> applicationFormList = new ArrayList<>();

        try {
          // 배치 단위로 Mock 데이터 생성
          for (int i = 0; i < size; i++) {
            ApplicationForm applicationForm = mockApplicationFormFactory.generate(agentList, clientList, concertList);
            applicationFormList.add(applicationForm);
          }

          // 트랜잭션 내에서 배치 저장
          transactionTemplate.execute(status -> {
            // 신청서 일괄 저장
            applicationFormRepository.saveAll(applicationFormList);

            // '거절'상태의 신청서에 대해서 거절사유 엔티티 저장
            List<RejectionReason> rejectionReasonList = applicationFormList.stream()
                .filter(form -> form.getApplicationFormStatus().equals(ApplicationFormStatus.REJECTED))
                .map(mockApplicationFormFactory::createRejectionReason)
                .collect(Collectors.toList());
            if (!CommonUtil.nullOrEmpty(rejectionReasonList)) {
              rejectionReasonRepository.saveAll(rejectionReasonList);
            }
            return null;
          });
        } catch (Exception e) {
          log.error("신청서 Mock 데이터 배치 생성 중 오류 발생: {}", e.getMessage());
          throw new CustomException(ErrorCode.GENERATE_MOCK_DATA_ERROR);
        }
      }, taskExecutor);
      futures.add(future);
    }

    // 모든 비동기 작업이 완료될 때까지 대기
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .handle((result, ex) -> {
          if (ex != null) {
            log.error("신청서 Mock 데이터 전체 저장 중 오류 발생: {}", ex.getMessage());
            throw new CustomException(ErrorCode.SAVE_MOCK_DATA_ERROR);
          }
          stopwatch.stop();
          log.debug("신청서 Mock 데이터 {}개 저장 완료, 소요시간: {}ms", total, stopwatch.getTotalTimeMillis());
          return null;
        });
  }

      /*
    ======================================채팅방======================================
     */

  /**
   * 채팅방 전용 클라이언트/에이전트 사용자를 생성합니다.
   * 또한 신청서세팅 + 채팅방 데이터를 생성합니다.
   */
  @Transactional
  public MockChatRoomResponse createChatRoomMockData() {
    // 캐시가 있고 아직 유효하면 그대로 반환
    CachedConfig cached = cachedConfigAtomicReference.get();

    if (!isCacheValid(cached)) {
      cachedConfigAtomicReference.set(null);
    }

    if (isCacheValid(cached)) {
      return cached.toResponse();
    }

    synchronized (this) {
      cached = cachedConfigAtomicReference.get();
      if (isCacheValid(cached)) {
        return cached.toResponse();
      }
    }

    MockLoginResponse agentLogin = testSocialLogin(
        MockLoginRequest.builder()
            .username(DEV_AGENT_USERNAME)
            .role(Role.ROLE_TEST)
            .memberType(MemberType.AGENT)
            .socialPlatform(SocialPlatform.KAKAO)
            .build()
    );
    log.debug("대리인 회원 생성 완료");

    MockLoginResponse clientLogin = testSocialLogin(
        MockLoginRequest.builder()
            .username(DEV_CLIENT_USERNAME)
            .role(Role.ROLE_TEST)
            .memberType(MemberType.CLIENT)
            .socialPlatform(SocialPlatform.NAVER)
            .build()
    );
    log.debug("의뢰인 회원 생성 완료");

    Member agent  = memberRepository.findById(agentLogin.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Member client = memberRepository.findById(clientLogin.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    List<Concert> concerts = concertRepository.findAll();
    if (concerts.isEmpty()) throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    Concert concert = concerts.get(0);

    ApplicationForm applicationForm = mockApplicationFormFactory.generate(List.of(agent), List.of(client), List.of(concert));

    applicationFormRepository.save(applicationForm);

    if (ApplicationFormStatus.REJECTED.equals(applicationForm.getApplicationFormStatus())) {
      rejectionReasonRepository.save(mockApplicationFormFactory.createRejectionReason(applicationForm));
    }

    ChatRoom chatRoom = ensureChatRoom(agent, client, concert, applicationForm);

    Date expAgentToken = tokenProvider.getExpiredAt(agentLogin.getAccessToken());
    Date expClientToken = tokenProvider.getExpiredAt(clientLogin.getAccessToken());
    Instant minExp = expAgentToken.toInstant()
        .isBefore(expClientToken.toInstant()) ? expAgentToken.toInstant() : expClientToken.toInstant();

    CachedConfig fresh = new CachedConfig(agentLogin.getAccessToken(), clientLogin.getAccessToken(),
        chatRoom.getChatRoomId(), minExp);
    cachedConfigAtomicReference.set(fresh);
    return fresh.toResponse();
  }

  private boolean isCacheValid(CachedConfig cached) {
    if (cached == null) return false;
    try {
      if (!tokenProvider.isValidToken(cached.agentToken()) ||
          !tokenProvider.isValidToken(cached.clientToken())) {
        return false;
      }

      // memberId 추출
      UUID agentId = UUID.fromString(tokenProvider.getMemberId(cached.agentToken()));
      UUID clientId = UUID.fromString(tokenProvider.getMemberId(cached.clientToken()));

      // DB에 정말 존재하는지 확인
      if (!memberRepository.existsById(agentId) || !memberRepository.existsById(clientId)) {
        return false;
      }

      // 만료 임박 토큰 방지
      Instant threshold = clock.instant().plusSeconds(60);
      return cached.expiresAt().isAfter(threshold);

    } catch (Exception e) {
      return false;
    }
  }


  private ChatRoom ensureChatRoom(Member agent, Member client, Concert concert, ApplicationForm applicationForm) {
    TicketOpenType ticketOpenType = applicationForm.getTicketOpenType();

    return chatRoomRepository
        .findByAgentMemberIdAndClientMemberIdAndConcertIdAndTicketOpenType(
            agent.getMemberId(), client.getMemberId(), concert.getConcertId(), ticketOpenType)
        .orElseGet(() -> {
          try {
            return chatRoomRepository.save(mockChatRoomFactory.generateFrom(applicationForm));
          } catch (org.springframework.dao.DuplicateKeyException e) {
            return chatRoomRepository
                .findByAgentMemberIdAndClientMemberIdAndConcertIdAndTicketOpenType(
                    agent.getMemberId(), client.getMemberId(), concert.getConcertId(), ticketOpenType)
                .orElseThrow();
          }
        });
  }
}
