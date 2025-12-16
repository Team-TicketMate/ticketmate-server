package com.ticketmate.backend.portfolio.application.service;

import static com.ticketmate.backend.portfolio.infrastructure.constant.PortfolioConstants.MAX_IMG_COUNT;

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
import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.PortfolioImg;
import com.ticketmate.backend.portfolio.infrastructure.repository.PortfolioRepository;
import com.ticketmate.backend.redis.application.annotation.RedisLock;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.model.FileMetadata;
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
@Slf4j
@RequiredArgsConstructor
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
  @RedisLock(key = "@redisLockKeyManager.generate('portfolio', #client.memberId)", leaseTime = 20L)
  public UUID uploadPortfolio(PortfolioRequest request, Member client) {

    // 검증
    memberService.validateMemberType(client, MemberType.CLIENT);
    validatePortfolioImgCount(request.getPortfolioImgList());

    return Optional.of(request)
        .map(req -> Portfolio.create(client, req.getPortfolioDescription(), PortfolioStatus.PENDING_REVIEW)) // dto 기반 Portfolio 엔티티 생성
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
   * 포트폴리오 이미지 처리
   */
  private Portfolio processPortfolioImgList(Portfolio portfolio, List<MultipartFile> imgList) {
    List<FileMetadata> metadataList = new ArrayList<>();

    try {
      for (MultipartFile file : imgList) {
        FileMetadata metadata = storageService.uploadFile(file, UploadType.PORTFOLIO);
        metadataList.add(metadata);
      }
    } catch (Exception e) {
      log.error("포트폴리오 이미지 업로드 중 오류 발생: {}", e.getMessage());
      log.error("이미 업로드 된 {} 개 파일 삭제 시도", metadataList.size());
      metadataList.forEach(metadata -> storageService.deleteFile(metadata.storedPath()));
      throw new CustomException(ErrorCode.PORTFOLIO_UPLOAD_ERROR);
    }
    for (FileMetadata metadata : metadataList) {
      PortfolioImg portfolioImg = PortfolioImg.of(portfolio, metadata);
      portfolio.addPortfolioImg(portfolioImg);
    }
    log.debug("포트폴리오 이미지 {}개 업로드 성공", metadataList.size());
    return portfolio;
  }

  /**
   * 요청된 첨부파일 개수 검증 (1~20개)
   */
  private void validatePortfolioImgCount(List<MultipartFile> imgList) {
    if (CommonUtil.nullOrEmpty(imgList) || imgList.size() > MAX_IMG_COUNT) {
      log.error("포트폴리오 이미지 첨부파일은 최소 1개 최대 20개까지 등록 가능합니다. 요청개수: {}", imgList.size());
      throw new CustomException(ErrorCode.INVALID_PORTFOLIO_IMG_COUNT);
    }
  }
}