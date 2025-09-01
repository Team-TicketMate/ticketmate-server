package com.ticketmate.backend.mock.application.service;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import com.ticketmate.backend.mock.application.dto.request.MockLoginRequest;
import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.PortfolioImg;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockPortfolioFactory {

  private final Faker koFaker;
  private final MockMemberFactory mockMemberFactory;
  private final MemberRepository memberRepository;

  /**
   * 포트폴리오 Mock 데이터 생성 (저장 X)
   */
  public Portfolio generate() {
    String portfolioDescription = koFaker.lorem().sentence(5, 10);
    Member client = createClientMember();

    Portfolio portfolio = Portfolio.builder()
        .portfolioDescription(portfolioDescription)
        .member(client)
        .portfolioImgList(new ArrayList<>())
        .portfolioStatus(koFaker.options().option(PortfolioStatus.class))
        .build();

    // 포트폴리오 이미지 추가 (양방향 관계 설정)
    List<PortfolioImg> portfolioImgList = generatePortfolioImgList(portfolio);
    if (!CommonUtil.nullOrEmpty(portfolioImgList)) {
      portfolioImgList.forEach(portfolio::addPortfolioImg);
    }
    return portfolio;
  }

  /**
   * 승인된 포트폴리오 Mock 데이터 생성 (저장 X) - 특정 대리인 Member와 연결
   */
  public Portfolio generate(Member agent) {
    String portfolioDescription = koFaker.lorem().sentence(5, 10);

    Portfolio portfolio = Portfolio.builder()
        .portfolioDescription(portfolioDescription)
        .member(agent)
        .portfolioImgList(new ArrayList<>())
        .portfolioStatus(PortfolioStatus.APPROVED)
        .build();

    // 포트폴리오 이미지 추가 (양방향 관계 설정)
    List<PortfolioImg> portfolioImgList = generatePortfolioImgList(portfolio);
    if (!CommonUtil.nullOrEmpty(portfolioImgList)) {
      portfolioImgList.forEach(portfolio::addPortfolioImg);
    }
    return portfolio;
  }

  /**
   * 포트폴리오 이미지 List Mock 데이터 생성 (0 ~ 20개 랜덤)
   */
  private List<PortfolioImg> generatePortfolioImgList(Portfolio portfolio) {
    int count = koFaker.random().nextInt(21);
    List<PortfolioImg> portfolioImgList = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      PortfolioImg portfolioImg = PortfolioImg.builder()
          .storedPath(koFaker.internet().image() + UUID.randomUUID())
          .build();
      portfolioImgList.add(portfolioImg);
    }

    return portfolioImgList;
  }

  // Mock 의뢰인 생성 및 저장
  private Member createClientMember() {
    Member member = mockMemberFactory.generate(MockLoginRequest.builder()
        .role(Role.ROLE_TEST)
        .memberType(MemberType.CLIENT)
        .build());
    return memberRepository.save(member);
  }
}
