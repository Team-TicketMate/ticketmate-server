package com.ticketmate.backend.service.admin;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.dto.admin.request.ConcertDateRequest;
import com.ticketmate.backend.object.dto.admin.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.admin.request.TicketOpenDateRequest;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.repository.postgres.concert.ConcertDateRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concert.TicketOpenDateRepository;
import com.ticketmate.backend.repository.postgres.concerthall.ConcertHallRepository;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioRepository;
import com.ticketmate.backend.service.concerthall.ConcertHallService;
import com.ticketmate.backend.service.fcm.FcmService;
import com.ticketmate.backend.service.file.FileService;
import com.ticketmate.backend.util.common.EntityMapper;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import com.ticketmate.backend.util.notification.NotificationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    AdminService adminService;

    @Mock
    ConcertHallService concertHallService;

    @Mock
    ConcertHallRepository concertHallRepository;

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
    FcmService fcmService;

    @Mock
    NotificationUtil notificationUtil;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void 공연_저장_성공() {

        // given
        UUID concertHallId = UUID.randomUUID();
        ConcertInfoRequest request = createConcertInfoRequest(concertHallId, "Test Concert Name");

        // when
        // 공연장 조회
        when(concertHallRepository.findById(concertHallId))
                .thenReturn(Optional.of(new ConcertHall()));

        // MultipartFile 저장
        when(fileService.saveFile(any()))
                .thenReturn("https://test.example.com/mock-file.png");

        // then
        adminService.saveConcertInfo(request);

        verify(concertRepository, times(1)).save(any(Concert.class));
        verify(concertDateRepository, times(1)).saveAll(any());
        verify(ticketOpenDateRepository, times(1)).saveAll(any());
        verify(fileService, times(2)).saveFile(any());
    }

    @Test
    void 공연_데이터_저장_null_허용되는_필드_확인() {
        // given
        ConcertInfoRequest request = createConcertInfoRequest(null, "Test Concert Name");

        // when
        request.setTicketReservationSite(null);
        request.setSeatingChart(null);

        // then
        adminService.saveConcertInfo(request);
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.PRE_OPEN_COUNT_EXCEED.getMessage());
    }

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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
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
        assertThatThrownBy(() -> adminService.saveConcertInfo(request))
                .isInstanceOf(CustomException.class);
    }

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
}