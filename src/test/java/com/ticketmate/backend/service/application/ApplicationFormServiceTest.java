package com.ticketmate.backend.service.application;

import com.ticketmate.backend.object.dto.application.request.ApplicationFormRequest;
import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concerthall.ConcertHallRepository;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import com.ticketmate.backend.service.test.TestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ticketmate.backend.object.constants.AccountStatus.ACTIVE_ACCOUNT;
import static com.ticketmate.backend.object.constants.MemberType.AGENT;
import static com.ticketmate.backend.object.constants.MemberType.CLIENT;
import static com.ticketmate.backend.object.constants.SocialPlatform.NAVER;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@Slf4j
class ApplicationFormServiceTest {

    @Autowired
    ApplicationFormService applicationFormService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ConcertHallRepository concertHallRepository;

    @Autowired
    ConcertRepository concertRepository;

    @Autowired
    TestService testService;

    private Member agent;
    private Member client;
    private Concert concert;
    private ConcertHall concertHall;

    @BeforeEach
    void setUp() {
        // 대리인 생성
        agent = testService.createMockMember(LoginRequest.builder()
                .socialPlatform(NAVER)
                .memberType(AGENT)
                .accountStatus(ACTIVE_ACCOUNT)
                .isFirstLogin(false)
                .build());

        // 의뢰인 생성
        client = testService.createMockMember(LoginRequest.builder()
                .socialPlatform(NAVER)
                .memberType(CLIENT)
                .accountStatus(ACTIVE_ACCOUNT)
                .isFirstLogin(false)
                .build());

        memberRepository.save(agent);
        memberRepository.save(client);

        // 공연장 생성
        concertHall = testService.createConcertHallMockData();
        concertHallRepository.save(concertHall);

        // 공연 생성
        concert = testService.createConcertMockData(Collections.singletonList(concertHall));
        concertRepository.save(concert);
    }

    @Test
    void main() {
        신청서_작성_성공();
    }

    void 신청서_작성_성공() {


        ApplicationFormRequest request = new ApplicationFormRequest();
        request.setAgentId(agent.getMemberId());
        request.setConcertId(concert.getConcertId());
        request.setRequestCount(1);
        request.setRequestDetails("요청사항입니다");
        request.setHopeAreaList();
    }
}