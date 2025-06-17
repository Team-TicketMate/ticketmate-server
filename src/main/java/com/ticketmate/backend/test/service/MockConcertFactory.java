package com.ticketmate.backend.test.service;

import com.ticketmate.backend.domain.concert.domain.constant.ConcertType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketReservationSite;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.global.util.common.CommonUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockConcertFactory {

  private final Faker koFaker;

  /**
   * 공연 단일 Mock 데이터를 생성 후 반환합니다. (저장 X)
   */
  public Concert generate(List<ConcertHall> concertHallList) {

    // 1. 공연 이름
    String concertName = koFaker.music().genre()
                         + " " + koFaker.team().name()
                         + koFaker.random().nextInt(10000)
                         + "공연";

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
  public List<ConcertDate> generateConcertDateList(Concert concert) {

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
  public List<TicketOpenDate> generalTicketOpenDateList(Concert concert) {
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
          .ticketOpenType(TicketOpenType.PRE_OPEN)
          .build();
      ticketOpenDateList.add(preOpen);
    }

    // 2. 일반 예매 오픈일 (선예매 오픈일이 없다면 필수 생성 / 선예매 오픈일이 있다면 50% 확률로 생성)
    if (CommonUtil.nullOrEmpty(ticketOpenDateList) || koFaker.random().nextBoolean()) {
      LocalDateTime generalOpenDate = baseDate.plusDays(koFaker.number().numberBetween(5, 10));
      TicketOpenDate generalOpen = TicketOpenDate.builder()
          .concert(concert)
          .openDate(generalOpenDate)
          .requestMaxCount(koFaker.number().numberBetween(1, 6))
          .isBankTransfer(koFaker.random().nextBoolean())
          .ticketOpenType(TicketOpenType.GENERAL_OPEN)
          .build();
      ticketOpenDateList.add(generalOpen);
    }

    return ticketOpenDateList;
  }
}
