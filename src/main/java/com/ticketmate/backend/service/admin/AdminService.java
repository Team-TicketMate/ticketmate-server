package com.ticketmate.backend.service.admin;

import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.dto.admin.request.PortfolioSearchRequest;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioRepository;
import com.ticketmate.backend.util.common.EntityMapper;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final PortfolioRepository portfolioRepository;
    private final EntityMapper entityMapper;
    @Value("${cloud.aws.s3.path.portfolio.cloud-front-domain}")
    private String portFolioDomain;

    /**
     * 페이지당 10개씩 관리자에게 포트폴리오 리스트 데이터를 보여줍니다.
     * 포트폴리오 정렬기준은 오래된순으로 정렬됩니다.
     */
    @Transactional(readOnly = true)
    public Page<PortfolioListForAdminResponse> getPortfolioList(PortfolioSearchRequest request) {

        int pageIndex = Math.max(1, request.getPage()) - 1;

        Pageable pageable = PageRequest.of(pageIndex,
                10,
                Sort.by("createdDate").ascending());

        Page<Portfolio> portfolioPage = portfolioRepository.findAllByPortfolioType(PortfolioType.UNDER_REVIEW, pageable);

        return portfolioPage.map(entityMapper::toPortfolioListForAdminResponse);
    }

    /**
     * 포트폴리오 상세조회 로직
     * @param portfolioId (UUID)
     */
    @Transactional
    public PortfolioForAdminResponse getPortfolio(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 검토중으로 update
        portfolio.setPortfolioType(PortfolioType.REVIEWING);

        PortfolioForAdminResponse portfolioForAdminResponse = entityMapper.toPortfolioForAdminResponse(portfolio);

        List<PortfolioImg> imgList = portfolio.getImgList();

        // 이미지 URL 파싱
        List<String> portfolioImgList = imgList.stream().map(img -> portFolioDomain + img.getImgName())
                .toList();

        portfolioForAdminResponse.addPortfolioImg(portfolioImgList);

        return portfolioForAdminResponse;
    }
}
