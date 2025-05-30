package com.ticketmate.backend.service.test;

import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import com.ticketmate.backend.util.common.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        Member client = mockMemberFactory.generate(LoginRequest.builder()
                .role(Role.ROLE_TEST)
                .memberType(MemberType.CLIENT)
                .build());
        memberRepository.save(client);

        Portfolio portfolio = Portfolio.builder()
                .portfolioDescription(portfolioDescription)
                .member(client)
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
                    .imgName(koFaker.internet().image())
                    .build();
            portfolioImgList.add(portfolioImg);
        }

        return portfolioImgList;
    }
}
