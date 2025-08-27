package com.ticketmate.backend.admin.portfolio.application.service;

import com.ticketmate.backend.admin.portfolio.application.dto.request.PortfolioFilteredRequest;
import com.ticketmate.backend.admin.portfolio.application.dto.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.mapper.PortfolioAdminMapper;
import com.ticketmate.backend.admin.portfolio.infrastructure.event.PortfolioHandledEvent;
import com.ticketmate.backend.admin.portfolio.infrastructure.repository.PortfolioRepositoryCustom;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.application.type.PortfolioNotificationType;
import com.ticketmate.backend.notification.core.service.NotificationService;
import com.ticketmate.backend.portfolio.application.service.PortfolioService;
import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioAdminService {

  private final PortfolioRepositoryCustom portfolioRepositoryCustom;
  @Qualifier("web")
  private final NotificationService notificationService;
  private final PortfolioService portfolioService;
  private final ApplicationEventPublisher publisher;
  private final PortfolioAdminMapper portfolioAdminMapper;

  /**
   * 페이지당 N개씩(기본10개) 반환합니다
   * 기본 정렬기준: 최신순
   */
  @Transactional(readOnly = true)
  public Page<PortfolioFilteredAdminResponse> filteredPortfolio(PortfolioFilteredRequest request) {
    return portfolioRepositoryCustom.filteredPortfolio(
        request.getUsername(),
        request.getNickname(),
        request.getName(),
        request.getPortfolioType(),
        request.toPageable()
    );
  }

  /**
   * 포트폴리오 상세조회 로직
   * 포트폴리오 PK를 입력받음
   * 해당 포트폴리오 상태를 IN_REVIEW(검토중) 으로 변경
   * 포트폴리오 대상 사용자 알림 발송
   *
   * @param portfolioId 포트폴리오 PK
   */
  @Transactional
  public PortfolioForAdminResponse getPortfolio(UUID portfolioId) {
    Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);

    // 포트폴리오가 "검토 대기" 상태인 경우
    if (portfolio.getPortfolioType().equals(PortfolioType.PENDING_REVIEW)) {
      // 포트폴리오 상태 "검토중" (IN_REVIEW)으로 변경
      portfolio.setPortfolioType(PortfolioType.REVIEWING);
    }
    return portfolioAdminMapper.toPortfolioForAdminResponse(portfolio);
  }

  /**
   * 관리자의 포트폴리오 승인 및 반려처리 로직
   *
   * @param portfolioId 포트폴리오 PK
   * @param request     portfolioId (UUID)
   *                    PortfolioType (포트폴리오 상태)
   */
  @Transactional
  public void reviewPortfolioCompleted(UUID portfolioId, PortfolioStatusUpdateRequest request) {
    Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
    Member client = portfolio.getMember();
    handlePortfolio(client, portfolio, request.getPortfolioType());
  }

  /**
   * 포트폴리오 알림 payload 생성
   */
  private NotificationPayload buildPortfolioNotificationPayload(Member member, PortfolioType portfolioType) {
    PortfolioNotificationType notificationType =
        CommonUtil.stringToEnum(PortfolioNotificationType.class, portfolioType.name());
    return notificationType.toPayload(member.getNickname());
  }

  /**
   * 포트폴리오 수락 or 거절
   *
   * @param member        회원
   * @param portfolio     포트폴리오
   * @param portfolioType 변경하려는 포트폴리오 상태
   */
  private void handlePortfolio(Member member, Portfolio portfolio, PortfolioType portfolioType) {
    if (!portfolio.getPortfolioType().equals(PortfolioType.REVIEWING)) {
      log.error("검토중인 상태의 포트폴리오만 승인 및 반려처리 가능합니다. 요청된 포트폴리오 상태: {}", portfolio.getPortfolioType());
      throw new CustomException(ErrorCode.INVALID_PORTFOLIO_TYPE);
    }

    // 포트폴리오 상태 업데이트
    portfolio.setPortfolioType(portfolioType);

    // '승인'인 경우 의뢰인 -> 대리인 변경
    if (portfolioType.equals(PortfolioType.APPROVED)) {
      portfolioService.promoteToAgent(portfolio);
      publisher.publishEvent(new PortfolioHandledEvent(portfolio.getPortfolioId(), portfolioType));
    }

    NotificationPayload payload = buildPortfolioNotificationPayload(member, portfolioType);

    notificationService.sendToMember(member.getMemberId(), payload);
    log.debug("포트폴리오: {}, {} 완료: {}", portfolio.getPortfolioId(), portfolioType.getDescription(), portfolioType);
  }
}
