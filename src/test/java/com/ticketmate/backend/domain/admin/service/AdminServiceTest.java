package com.ticketmate.backend.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketmate.backend.domain.admin.dto.request.ConcertDateRequest;
import com.ticketmate.backend.domain.admin.dto.request.ConcertHallInfoRequest;
import com.ticketmate.backend.domain.admin.dto.request.ConcertInfoRequest;
import com.ticketmate.backend.domain.admin.dto.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.domain.admin.dto.request.TicketOpenDateRequest;
import com.ticketmate.backend.domain.admin.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.domain.concert.domain.constant.ConcertType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketReservationSite;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.repository.ConcertDateRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepository;
import com.ticketmate.backend.domain.concert.repository.TicketOpenDateRepository;
import com.ticketmate.backend.domain.concert.service.ConcertDateService;
import com.ticketmate.backend.domain.concert.service.TicketOpenDateService;
import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.concerthall.repository.ConcertHallRepository;
import com.ticketmate.backend.domain.concerthall.service.ConcertHallService;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.service.FcmTokenService;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.portfolio.domain.entity.PortfolioImg;
import com.ticketmate.backend.domain.portfolio.repository.PortfolioRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.file.service.FileService;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @InjectMocks
  AdminService adminService;

  @Mock
  ConcertHallService concertHallService;

  @Mock
  ConcertHallRepository concertHallRepository;

  @Mock
  ConcertDateService concertDateService;

  @Mock
  TicketOpenDateService ticketOpenDateService;

  @Mock
  ConcertRepository concertRepository;

  @Mock
  ConcertDateRepository concertDateRepository;

  @Mock
  TicketOpenDateRepository ticketOpenDateRepository;

  @Mock
  PortfolioRepository portfolioRepository;

  @Mock
  FileService fileService;

  @Mock
  EntityMapper entityMapper;

  @Mock
  FcmTokenService fcmTokenService;

  @Mock
  NotificationUtil notificationUtil;

  private static ConcertInfoRequest createConcertInfoRequest(UUID concertHallId, String concertName) {
    return ConcertInfoRequest.builder()
        .concertName(concertName)
        .concertHallId(concertHallId)
        .concertType(ConcertType.CONCERT)
        .concertThumbNail(new MockMultipartFile(
            "thumbnail", "thumbnail.png", "image/png", new byte[]{1, 2, 3}
        ))
        .seatingChart(new MockMultipartFile(
            "seatingChart", "seatingChart.png", "image/png", new byte[]{1, 2, 3}
        ))
        .ticketReservationSite(TicketReservationSite.INTERPARK_TICKET)
        .concertDateRequestList(List.of(
            ConcertDateRequest.builder()
                .performanceDate(LocalDateTime.of(2025, 6, 1, 18, 0))
                .session(1)
                .build(),
            ConcertDateRequest.builder()
                .performanceDate(LocalDateTime.of(2025, 6, 2, 18, 0))
                .session(2)
                .build()
        ))
        .ticketOpenDateRequestList(List.of(
            TicketOpenDateRequest.builder()
                .openDate(LocalDateTime.of(2025, 5, 1, 18, 0))
                .requestMaxCount(10)
                .isBankTransfer(true)
                .ticketOpenType(TicketOpenType.PRE_OPEN)
                .build(),
            TicketOpenDateRequest.builder()
                .openDate(LocalDateTime.of(2025, 5, 2, 18, 0))
                .requestMaxCount(10)
                .isBankTransfer(true)
                .ticketOpenType(TicketOpenType.GENERAL_OPEN)
                .build()
        ))
        .build();
  }

  private static ConcertHallInfoRequest createConcertHallInfoRequest(String concertHallName, String address, String webSiteUrl) {
    return ConcertHallInfoRequest.builder()
        .concertHallName(concertHallName)
        .address(address)
        .webSiteUrl(webSiteUrl)
        .build();
  }

  private static Portfolio createPortfolio(UUID portfolioId, UUID memberId, PortfolioType portfolioType) {
    Portfolio portfolio = Portfolio.builder()
        .portfolioId(portfolioId)
        .portfolioDescription("포트폴리오 소개")
        .portfolioType(portfolioType)
        .member(Member.builder()
            .memberId(memberId)
            .memberType(MemberType.CLIENT)
            .build())
        .build();

    PortfolioImg portfolioImg = PortfolioImg.builder()
        .portfolioImgId(UUID.randomUUID())
        .filePath("https://test-image.png.com")
        .portfolio(portfolio)
        .build();

    portfolio.addImg(portfolioImg);
    return portfolio;
  }

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  /*
  --------------------------------------공연 관련 테스트--------------------------------------
   */
  @Test
  void 공연_저장_성공() {

    // given
    UUID concertHallId = UUID.randomUUID();
    ConcertInfoRequest request = createConcertInfoRequest(concertHallId, "Test Concert Name");

    // when
    // 공연장 조회
//    when(concertHallRepository.findById(concertHallId))
//        .thenReturn(Optional.of(new ConcertHall()));

    // MultipartFile 저장
    when(fileService.uploadFile(any(), any()))
        .thenReturn("https://test.example.com/mock-file.png");

    // then
    adminService.saveConcert(request);

    verify(concertRepository, times(1)).save(any(Concert.class));
    verify(concertDateRepository, times(1)).saveAll(any());
    verify(ticketOpenDateRepository, times(1)).saveAll(any());
    verify(fileService, times(2)).uploadFile(any(), any());
  }

  @Test
  void 공연_데이터_저장_null_허용되는_필드_확인() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");

    // when
    request.setTicketReservationSite(null);
    request.setSeatingChart(null);

    // then
    adminService.saveConcert(request);
    verify(concertRepository, times(1)).save(any(Concert.class));
  }

  @Test
  void 공연_이름_중복_저장_실패() {
    // given
    when(concertRepository.existsByConcertName("Duplicate Concert Name"))
        .thenReturn(true);

    // when
    ConcertInfoRequest request = createConcertInfoRequest(null, "Duplicate Concert Name");

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.DUPLICATE_CONCERT_NAME.getMessage());
  }

  @Test
  void 공연_날짜_null_입력_오류_발생() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setConcertDateRequestList(null);

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.CONCERT_DATE_REQUIRED.getMessage());
  }

  @Test
  void 공연_날짜_내부_회차_중복() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setConcertDateRequestList(List.of(
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 1, 18, 0))
            .session(1)
            .build(),
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 2, 18, 0))
            .session(1)
            .build()
    ));

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_CONCERT_DATE.getMessage());
  }

  @Test
  void 공연_날짜_내부_회차_건너뜀() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setConcertDateRequestList(List.of(
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 1, 18, 0))
            .session(1)
            .build(),
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 2, 18, 0))
            .session(3)
            .build()
    ));

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_CONCERT_DATE.getMessage());
  }

  @Test
  void 공연_날짜_회차_2회차_시작됨() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setConcertDateRequestList(List.of(
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 1, 18, 0))
            .session(2)
            .build(),
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 2, 18, 0))
            .session(3)
            .build()
    ));

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_CONCERT_DATE.getMessage());
  }

  @Test
  void 공연_날짜가_빠른_데이터가_회차가_더_늦는_경우_오류() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setConcertDateRequestList(List.of(
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 1, 18, 0))
            .session(2)
            .build(),
        ConcertDateRequest.builder()
            .performanceDate(LocalDateTime.of(2025, 6, 2, 18, 0))
            .session(1)
            .build()
    ));

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_CONCERT_DATE.getMessage());
  }

  @Test
  void 티켓_오픈일_null_입력_오류_발생() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setTicketOpenDateRequestList(null);

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.TICKET_OPEN_DATE_REQUIRED.getMessage());
  }

  @Test
  void 티켓_오픈일_선예매_중복_입력_오류_발생() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setTicketOpenDateRequestList(List.of(
        TicketOpenDateRequest.builder()
            .openDate(LocalDateTime.of(2025, 5, 1, 18, 0))
            .requestMaxCount(10)
            .isBankTransfer(true)
            .ticketOpenType(TicketOpenType.PRE_OPEN)
            .build(),
        TicketOpenDateRequest.builder()
            .openDate(LocalDateTime.of(2025, 5, 2, 18, 0))
            .requestMaxCount(10)
            .isBankTransfer(true)
            .ticketOpenType(TicketOpenType.PRE_OPEN)
            .build()
    ));

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.PRE_OPEN_COUNT_EXCEED.getMessage());
  }

    /*
    --------------------------------------공연장 관련 테스트--------------------------------------
     */

  @Test
  void 티켓_오픈일_일반예매_중복_입력_오류_발생() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setTicketOpenDateRequestList(List.of(
        TicketOpenDateRequest.builder()
            .openDate(LocalDateTime.of(2025, 5, 1, 18, 0))
            .requestMaxCount(10)
            .isBankTransfer(true)
            .ticketOpenType(TicketOpenType.GENERAL_OPEN)
            .build(),
        TicketOpenDateRequest.builder()
            .openDate(LocalDateTime.of(2025, 5, 2, 18, 0))
            .requestMaxCount(10)
            .isBankTransfer(true)
            .ticketOpenType(TicketOpenType.GENERAL_OPEN)
            .build()
    ));

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.GENERAL_OPEN_COUNT_EXCEED.getMessage());
  }

  @Test
  void 티켓_최대_요청_개수_0개_입력_오류_발생() {
    // given
    ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");
    request.setTicketOpenDateRequestList(List.of(
        TicketOpenDateRequest.builder()
            .openDate(LocalDateTime.of(2025, 5, 1, 18, 0))
            .requestMaxCount(0)
            .isBankTransfer(true)
            .ticketOpenType(TicketOpenType.PRE_OPEN)
            .build(),
        TicketOpenDateRequest.builder()
            .openDate(LocalDateTime.of(2025, 5, 2, 18, 0))
            .requestMaxCount(10)
            .isBankTransfer(true)
            .ticketOpenType(TicketOpenType.GENERAL_OPEN)
            .build()
    ));

    // when

    // then
    assertThatThrownBy(() -> adminService.saveConcert(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_TICKET_REQUEST_MAX_COUNT.getMessage());
  }

  @Test
  void 공연장_저장_성공() {
    // given
    ConcertHallInfoRequest request = createConcertHallInfoRequest(
        "테스트 공연장",
        "서울특별시 중구 동호로 241",
        "https://www.ticketmate.com"
    );
    request.setAddress(null);
    request.setWebSiteUrl(null);

    // when
    adminService.saveConcertHallInfo(request);

    // then
    verify(concertHallRepository, times(1)).save(any(ConcertHall.class));
  }

  @Test
  void 공연장_저장_실패_중복_공연장명() {
    // given
    ConcertHallInfoRequest request = createConcertHallInfoRequest(
        "테스트 공연장",
        "서울특별시 중구 동호로 241",
        "https://www.ticketmate.com"
    );

    // when
    when(concertHallRepository.existsByConcertHallName("테스트 공연장")).thenReturn(true);

    // then
    assertThatThrownBy(() -> adminService.saveConcertHallInfo(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.DUPLICATE_CONCERT_HALL_NAME.getMessage());
  }

    /*
    --------------------------------------포트폴리오 관련 테스트--------------------------------------
     */

  @Test
  void 공연장_주소에_따른_지역코드_확인() {
    // given
    ArgumentCaptor<ConcertHall> captor = ArgumentCaptor.forClass(ConcertHall.class);

    ConcertHallInfoRequest request = createConcertHallInfoRequest(
        "테스트 공연장",
        "서울특별시 중구 동호로 241",
        "https://www.ticketmate.com"
    );

    // when
    adminService.saveConcertHallInfo(request);

    // then
    verify(concertHallRepository).save(captor.capture());
    ConcertHall savedConcertHall = captor.getValue();
    assertThat(savedConcertHall.getCity()).isEqualTo(City.SEOUL);
    assertThat(savedConcertHall.getCity().getCityCode()).isEqualTo(11);
  }

  @Test
  void 포트폴리오_상세_조회_성공_검토중_업데이트() {
    // given
    UUID portfolioId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    Portfolio portfolio = createPortfolio(portfolioId, memberId, PortfolioType.PENDING_REVIEW);

    // when
    when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
    when(entityMapper.toPortfolioForAdminResponse(portfolio))
        .thenReturn(PortfolioForAdminResponse.builder()
            .portfolioId(portfolioId)
            .build());

    // then
    PortfolioForAdminResponse response = adminService.getPortfolio(portfolioId);

    assertThat(response).isNotNull();
    assertThat(response.getPortfolioId()).isEqualTo(portfolioId);
    verify(portfolioRepository).findById(portfolioId);

    // 포트폴리오 상태가 IN_REVIEW로 변경되었는지 확인
    assertThat(portfolio.getPortfolioType()).isEqualTo(PortfolioType.REVIEWING);

    // 알림 전송 확인
//    verify(notificationUtil).portfolioNotification(eq(PortfolioType.REVIEWING), eq(portfolio));
//    verify(fcmTokenService).sendNotification(eq(memberId), any());
  }

  @Test
  void 포트폴리오_상세_조회_성공_검토중_업데이트_및_알림전송_안됨() {
    // given
    UUID portfolioId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    Portfolio portfolio = createPortfolio(portfolioId, memberId, PortfolioType.APPROVED);

    // when
    when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
    when(entityMapper.toPortfolioForAdminResponse(portfolio))
        .thenReturn(PortfolioForAdminResponse.builder()
            .portfolioId(portfolioId)
            .build());

    // then
    PortfolioForAdminResponse response = adminService.getPortfolio(portfolioId);

    assertThat(response).isNotNull();
    assertThat(response.getPortfolioId()).isEqualTo(portfolioId);
    verify(portfolioRepository).findById(portfolioId);

    // 포트폴리오 상태가 IN_REVIEW로 변경되지 않았는지 확인
    assertThat(portfolio.getPortfolioType()).isNotEqualTo(PortfolioType.REVIEWING);
    assertThat(portfolio.getPortfolioType()).isEqualTo(PortfolioType.APPROVED);

    // 알림 전송 확인
//    verify(notificationUtil, never()).portfolioNotification(eq(PortfolioType.REVIEWING), eq(portfolio));
//    verify(fcmTokenService, never()).sendNotification(eq(memberId), any());
  }

  @Test
  void 관리자_포트폴리오_승인_성공() {
    // given
    UUID portfolioId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    Portfolio portfolio = createPortfolio(portfolioId, memberId, PortfolioType.REVIEWING);

    when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
    PortfolioStatusUpdateRequest request = PortfolioStatusUpdateRequest.builder()
        .portfolioType(PortfolioType.APPROVED)
        .build();

    // when
//    UUID returnId = adminService.reviewPortfolioCompleted(portfolioId, request);

    // then
//    assertThat(returnId).isEqualTo(portfolioId);
//    assertThat(portfolio.getPortfolioType()).isEqualTo(PortfolioType.APPROVED);
//    assertThat(portfolio.getMember().getMemberType()).isEqualTo(MemberType.AGENT);
//    verify(notificationUtil).portfolioNotification(PortfolioType.APPROVED, eq(portfolio));
//    verify(fcmTokenService).sendNotification(eq(memberId), any());
  }

  @Test
  void 포트폴리오_승인_반려_에러_발생_검토중이_아닌_포트폴리오_요청() {

  }

  @Test
  void 포트폴리오_승인_반려_에러_발생_유효하지_않은_포트폴리오_타입_요청() {

  }
}