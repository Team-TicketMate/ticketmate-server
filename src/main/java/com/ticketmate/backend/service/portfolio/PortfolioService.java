package com.ticketmate.backend.service.portfolio;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.dto.portfolio.request.PortfolioRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioRepository;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.path.portfolio.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.path.portfolio.cloud-front-domain}")
    private String domain;
    private static final Integer MAX_IMAGE_COUNT = 20;

    /**
     * 포트폴리오 업로드
     *
     * @param request publicRelations
     * @return 생성된 포트폴리오에 대한 고유한 UUID
     */
    @Transactional
    public UUID uploadPortfolio(PortfolioRequest request, Member member){
        if (request.getPortfolioImg().size() > MAX_IMAGE_COUNT) {
            throw new CustomException(ErrorCode.PORTFOLIO_IMG_MAX_COUNT_EXCEEDED);
        }

        Portfolio portfolio = Portfolio.builder()
                .member(member)
                .publicRelations(request.getPublicRelations())
                .portfolioType(PortfolioType.UNDER_REVIEW)
                .build();

        /**
         * Cascade 옵션을 활용해서 부모 엔티티만 Save 해서 update 쿼리 최소화
         */
        if (request.getPortfolioImg().size() > 0) {
            for (MultipartFile imgFile : request.getPortfolioImg()) {
                String fileName = s3Upload(imgFile, portfolio);

                PortfolioImg portfolioImg = PortfolioImg.builder()
                        .imgName(fileName)
                        .portfolio(portfolio)
                        .build();

                portfolio.addImg(portfolioImg);
            }
        }

        portfolioRepository.save(portfolio);

        log.debug("총 저장된 파일 갯수 : {}", portfolio.getImgList().size());

        return portfolio.getPortfolioId();
    }


    /**
     * S3 버킷에 이미지를 저장하는 로직입니다.
     */
    @Transactional
    public String s3Upload(MultipartFile portfolioImg, Portfolio portfolio){
        String originalFilename = portfolioImg.getOriginalFilename();

        getFileExtension(originalFilename);

        // 중복이 되지 않는 고유한 파일이름 생성
        String randomFilename = generateRandomFilename(originalFilename);
        log.debug("filename: {}", randomFilename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(portfolioImg.getSize());
        metadata.setContentType(portfolioImg.getContentType());

        try {
            amazonS3.putObject(bucket, randomFilename, portfolioImg.getInputStream(), metadata);

        } catch (AmazonServiceException | IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
        }

        String s3UrlString = amazonS3.getUrl(bucket, randomFilename).toString();
        log.debug("S3URL: {}", s3UrlString);
        String cloudFrontUrl = domain + randomFilename;

        log.debug("ImgURL: {}", cloudFrontUrl);

        PortfolioImg img = PortfolioImg.builder()
                .imgName(randomFilename)
                .portfolio(portfolio)
                .build();

        log.debug("Img : {}", img.getImgName());
        return randomFilename;
    }

    /**
     * 파일 이름 생성을 위한 메서드입니다.
     */
    private String generateRandomFilename(String fileName) {
        return UUID.randomUUID() + fileName;
    }

    /**
     * 이미지 파일만 저장될 수 있도록 파일의 유효성을 검사하는 메서드입니다.
     */
    private void getFileExtension(String fileName) {
        if (fileName.length() == 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_IMAGE);
        }
        ArrayList<String> fileValidate = new ArrayList<>();
        fileValidate.add(".jpg");
        fileValidate.add(".jpeg");
        fileValidate.add(".png");
        fileValidate.add(".JPG");
        fileValidate.add(".JPEG");
        fileValidate.add(".PNG");
        String idxFileName = fileName.substring(fileName.lastIndexOf("."));
        if (!fileValidate.contains(idxFileName)) {
            throw new CustomException(ErrorCode.WRONG_IMAGE_FORMAT);
        }
    }
}