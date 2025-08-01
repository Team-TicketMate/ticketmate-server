package com.ticketmate.backend.portfolio.application.service;

import com.ticketmate.backend.ai.application.service.VertexAiEmbeddingService;
import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.AgentPerformanceSummaryRepository;
import com.ticketmate.backend.portfolio.application.dto.request.PortfolioRequest;
import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.PortfolioImg;
import com.ticketmate.backend.portfolio.infrastructure.repository.PortfolioRepository;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.service.StorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  private final StorageService storageService;
  private final AgentPerformanceSummaryRepository agentPerformanceSummaryRepository;
  private final VertexAiEmbeddingService vertexAiEmbeddingService;

  /**
   * 포트폴리오 업로드
   *
   * @param request portfolioDescription 자기소개
   *                portfolioImgList 첨부파일 이미지 리스트
   */
  @Transactional
  public UUID uploadPortfolio(PortfolioRequest request, Member client) {

    // 검증
    memberService.validateMemberType(client, MemberType.CLIENT);
    validatePortfolioImgCount(request.getPortfolioImgList());

    return Optional.of(request)
        .map(req -> createPortfolio(req, client)) // dto 기반 Portfolio 엔티티 생성
        .map(portfolio -> processPortfolioImgList(portfolio, request.getPortfolioImgList()))
        .map(portfolioRepository::save)
        .map(Portfolio::getPortfolioId)
        .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_UPLOAD_ERROR));
  }

  /**
   * portfolioId 해당하는 포트폴리오 반환
   *
   * @param portfolioId 포트폴리오 PK
   */
  public Portfolio findPortfolioById(UUID portfolioId) {
    return portfolioRepository.findById(portfolioId)
        .orElseThrow(() -> {
          log.error("요청한 PK값에 해당하는 포트폴리오를 찾을 수 없습니다. 요청 PK: {}", portfolioId);
          return new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND);
        });
  }

  /**
   * 의뢰인 -> 대리인 MemberType 변경
   *
   * @param portfolio MemberType을 변경하려는 Member의 portfolio
   */
  @Transactional
  public void promoteToAgent(Portfolio portfolio) {
    Member member = portfolio.getMember();
    member.setMemberType(MemberType.AGENT);

    if (!agentPerformanceSummaryRepository.existsById(member.getMemberId())) {
      AgentPerformanceSummary summary = AgentPerformanceSummary.builder()
          .agent(member)
          .totalScore(0.0)
          .averageRating(0.0)
          .reviewCount(0)
          .recentSuccessCount(0)
          .build();
      agentPerformanceSummaryRepository.save(summary);
    }

    // 임베딩 저장 Text 생성 (닉네임 + 한줄 소개)
    String embeddingText = CommonUtil.combineTexts(
        member.getNickname(), portfolio.getPortfolioDescription()
    );
    // 임베딩 저장
    vertexAiEmbeddingService.fetchOrGenerateEmbedding(
        member.getMemberId(),
        embeddingText,
        EmbeddingType.AGENT
    );
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
  private Portfolio processPortfolioImgList(Portfolio portfolio, List<MultipartFile> imgList) {
    List<String> filePathList = new ArrayList<>();

    try {
      imgList.stream()
          .map(file -> storageService.uploadFile(file, UploadType.PORTFOLIO)) // 파일 업로드 -> filePath 반환
          .peek(filePathList::add) // rollback 용도로 list에 저장
          .map(filePath -> createPortfolioImg(portfolio, filePath)) // filePath -> PortfolioImg 생성
          .forEach(portfolio::addImg); // 양방향 연관관계 처리
      log.debug("총 저장된 포트폴리오 이미지 파일 개수: {}", imgList.size());
      return portfolio;
    } catch (Exception e) {
      log.error("포트폴리오 이미지 업로드 중 오류 발생: {}, 이미 업로드 된 {} 개 파일 삭제", e.getMessage(), filePathList.size());
      filePathList.forEach(storageService::deleteFile);
      throw new CustomException(ErrorCode.PORTFOLIO_UPLOAD_ERROR);
    }
  }

  /**
   * 포트폴리오 이미지 엔티티 생성
   */
  private PortfolioImg createPortfolioImg(Portfolio portfolio, String filePath) {
    return PortfolioImg.builder()
        .portfolio(portfolio)
        .filePath(filePath)
        .build();
  }

  /**
   * 요청된 첨부파일 개수 검증 (1~20개)
   */
  private void validatePortfolioImgCount(List<MultipartFile> imgList) {
    if (CommonUtil.nullOrEmpty(imgList) || imgList.size() > Portfolio.MAX_IMG_COUNT) {
      log.error("포트폴리오 이미지 첨부파일은 최소 1개 최대 20개까지 등록가능합니다. 요청개수: {}", imgList.size());
      throw new CustomException(ErrorCode.INVALID_PORTFOLIO_IMG_COUNT);
    }
  }
}