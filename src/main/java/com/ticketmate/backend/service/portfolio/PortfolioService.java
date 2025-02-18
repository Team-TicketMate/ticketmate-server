package com.ticketmate.backend.service.portfolio;

import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.dto.portfolio.request.PortfolioRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioImgRepository;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioRepository;
import com.ticketmate.backend.service.s3.S3Service;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioImgRepository portfolioImgRepository;
    private final S3Service s3Service;
    private static final Integer MAX_IMAGE_COUNT = 20;

    /**
     * 포트폴리오 업로드
     *
     * @param request publicRelations
     * @return 생성된 포트폴리오에 대한 고유한 UUID
     */
    @Transactional
    public UUID uploadPortfolio(PortfolioRequest request, Member member){
        if (request.getPortfolioImgs().size() > MAX_IMAGE_COUNT) {
            throw new CustomException(ErrorCode.PORTFOLIO_IMG_MAX_COUNT_EXCEEDED);
        }

        Portfolio portfolio = Portfolio.builder()
                .member(member)
                .portfolioDescription(request.getPortfolioDescription())
                .portfolioType(PortfolioType.UNDER_REVIEW)
                .build();

        portfolioRepository.save(portfolio);

        if (request.getPortfolioImgs().size() > 0) {
            List<PortfolioImg> portfolioImgList = new ArrayList<>();
            for (MultipartFile imgFile : request.getPortfolioImgs()) {
                String fileName = uploadPortfolioImage(imgFile, portfolio);

                PortfolioImg portfolioImg = PortfolioImg.builder()
                        .imgName(fileName)
                        .portfolio(portfolio)
                        .build();

                portfolioImgList.add(portfolioImg);
                portfolio.addImg(portfolioImg);
            }
            portfolioImgRepository.saveAll(portfolioImgList);

            log.debug("총 저장된 이미지 파일 갯수 : {}", portfolioImgList.size());
        }

        return portfolio.getPortfolioId();
    }

    @Transactional
    public String uploadPortfolioImage(MultipartFile portfolioImg, Portfolio portfolio){
        String randomFilename = s3Service.s3UploadImgForCloudFront(portfolioImg);

        PortfolioImg img = PortfolioImg.builder()
                .imgName(randomFilename)
                .portfolio(portfolio)
                .build();

        log.debug("Img : {}", img.getImgName());
        return randomFilename;
    }
}