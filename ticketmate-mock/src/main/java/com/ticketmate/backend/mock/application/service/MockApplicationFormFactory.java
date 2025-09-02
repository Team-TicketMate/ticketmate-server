package com.ticketmate.backend.mock.application.service;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationFormDetail;
import com.ticketmate.backend.applicationform.infrastructure.entity.HopeArea;
import com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertDateRepository;
import com.ticketmate.backend.concert.infrastructure.repository.TicketOpenDateRepository;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockApplicationFormFactory {

  private final Faker koFaker;
  private final ConcertDateRepository concertDateRepository;
  private final TicketOpenDateRepository ticketOpenDateRepository;


  /**
   * 신청서 단일 Mock 데이터를 생성합니다 (저장 X)
   *
   * @param agentList   DB에 저장된 대리인 리스트
   * @param clientList  DB에 저장된 의뢰인 리스트
   * @param concertList DB에 저장된 콘서트 리스트
   * @return 생성된 신청서 Mock데이터
   */
  public ApplicationForm generate(List<Member> agentList, List<Member> clientList, List<Concert> concertList) {

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

    // 신청서 세부사항 추가 (양방향 연관관계)
    createApplicationFormDetailList(concert, ticketOpenType)
        .forEach(applicationForm::addApplicationFormDetail);

    return applicationForm;
  }

  /**
   * 신청서 세부사항 Mock 데이터를 생성합니다
   */
  private List<ApplicationFormDetail> createApplicationFormDetailList(Concert concert, TicketOpenType ticketOpenType) {
    List<ConcertDate> concertDateList = concertDateRepository.findAllByConcertConcertIdOrderByPerformanceDateAsc(concert.getConcertId());
    if (CommonUtil.nullOrEmpty(concertDateList)) {
      log.error("신청서 세부사항 Mock 데이터 생성 중 공연: {}에 해당하는 공연 날짜가 존재하지 않습니다.", concert.getConcertName());
      throw new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND);
    }
    int size = koFaker.random().nextInt(1, concertDateList.size());
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
      int requestCount = koFaker.random().nextInt(1, ticketOpenDate.getRequestMaxCount());

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
          .price(koFaker.number().numberBetween(1, 21) * 10000)
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

  /**
   * 신청서 거절사유 생성
   * 거절사유가 'OTHER'이면 otherMemo 5자 이상 생성
   */
  public RejectionReason createRejectionReason(ApplicationForm applicationForm) {
    return RejectionReason.builder()
        .applicationForm(applicationForm)
        .applicationFormRejectedType(koFaker.options().option(ApplicationFormRejectedType.class))
        .otherMemo(koFaker.lorem().sentence(3, 5))
        .build();
  }
}
