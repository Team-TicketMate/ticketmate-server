package com.ticketmate.backend.test.service;

import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import com.ticketmate.backend.domain.member.domain.constant.Role;
import com.ticketmate.backend.test.dto.request.LoginRequest;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.portfolio.domain.entity.PortfolioImg;
import com.ticketmate.backend.domain.member.repository.MemberRepository;
import com.ticketmate.backend.global.util.common.CommonUtil;
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
        .portfolioType(koFaker.options().option(PortfolioType.class))
        .build();

    // 포트폴리오 이미지 추가 (양방향 관계 설정)
    List<PortfolioImg> portfolioImgList = generatePortfolioImgList(portfolio);
    if (!CommonUtil.nullOrEmpty(portfolioImgList)) {
      portfolioImgList.forEach(portfolio::addImg);
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
          .filePath(koFaker.internet().image())
          .build();
      portfolioImgList.add(portfolioImg);
    }

    return portfolioImgList;
  }

  // Mock 의뢰인 생성 및 저장
  private Member createClientMember() {
    Member member = mockMemberFactory.generate(LoginRequest.builder()
        .role(Role.ROLE_TEST)
        .memberType(MemberType.CLIENT)
        .build());
    return memberRepository.save(member);
  }
}
