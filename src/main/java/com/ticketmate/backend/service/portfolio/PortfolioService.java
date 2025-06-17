package com.ticketmate.backend.service.portfolio;

import static com.ticketmate.backend.object.postgres.portfolio.Portfolio.MAX_IMG_COUNT;

import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.constants.UploadType;
import com.ticketmate.backend.object.dto.portfolio.request.PortfolioRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.repository.postgres.portfolio.PortfolioRepository;
import com.ticketmate.backend.service.member.MemberService;
import com.ticketmate.backend.service.storage.FileService;
import com.ticketmate.backend.util.common.CommonUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

  private final PortfolioRepository portfolioRepository;
  private final MemberService memberService;
  private final FileService fileService;

  /**
   * 포트폴리오 업로드
   *
   * @param request publicRelations
   * @return 생성된 포트폴리오에 대한 고유한 UUID
   */
  @Transactional
  public UUID uploadPortfolio(PortfolioRequest request, Member client) {

    // 요청값 검증
    validatePortfolioRequest(request, client);

    // 포트폴리오 엔티티 생성
    Portfolio portfolio = createPortfolio(request, client);

    if (!CommonUtil.nullOrEmpty(request.getPortfolioImgList())) {
      processPortfolioImgList(portfolio, request.getPortfolioImgList());
    }

    return portfolioRepository.save(portfolio).getPortfolioId();
  }

  /**
   * 포트폴리오 요청 검증
   */
  private void validatePortfolioRequest(PortfolioRequest request, Member client) {
    memberService.validateMemberType(client, MemberType.CLIENT);

    if (request.getPortfolioImgList().size() > MAX_IMG_COUNT) {
      log.error("포트폴리오 이미지 첨부파일 개수가 {}를 초과했습니다. 첨부파일 개수: {}", MAX_IMG_COUNT, request.getPortfolioImgList().size());
      throw new CustomException(ErrorCode.PORTFOLIO_IMG_MAX_COUNT_EXCEEDED);
    }
  }

  /**
   * 포트폴리오 엔티티 생성
   */
  private Portfolio createPortfolio(PortfolioRequest request, Member client) {
    return Portfolio.builder()
        .member(client)
        .portfolioDescription(request.getPortfolioDescription())
        .portfolioImgList(new ArrayList<>())
        .portfolioType(PortfolioType.PENDING_REVIEW)
        .build();
  }

  /**
   * 포트폴리오 이미지 처리
   */
  private void processPortfolioImgList(Portfolio portfolio, List<MultipartFile> imgList) {
    List<String> filePathList = new ArrayList<>();
    int uploadCount = 0;

    try {
      for (MultipartFile file : imgList) {
        String filePath = fileService.uploadFile(file, UploadType.PORTFOLIO);
        filePathList.add(filePath);

        PortfolioImg portfolioImg = PortfolioImg.builder()
            .portfolio(portfolio)
            .filePath(filePath)
            .build();

        portfolio.addImg(portfolioImg);
        uploadCount++;
      }
      log.debug("총 저장된 포트폴리오 이미지 파일 개수: {}", uploadCount);
    } catch (Exception e) {
      log.error("포트폴리오 이미지 업로드 중 오류 발생, 이미 업로드 된 {} 개 파일 삭제", filePathList.size());

      // 이미 업로드 된 파일들 모두 삭제
      for (String filePath : filePathList) {
        try {
          fileService.deleteFile(filePath);
        } catch (Exception exception) {
          log.error("롤백 중 파일 삭제 실패: {}, 오류: {}", filePath, exception.getMessage());
        }
      }
      throw e;
    }
  }
}