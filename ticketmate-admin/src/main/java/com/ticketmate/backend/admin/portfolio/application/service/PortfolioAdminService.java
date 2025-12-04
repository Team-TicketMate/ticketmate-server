package com.ticketmate.backend.admin.portfolio.application.service;

import com.ticketmate.backend.admin.portfolio.application.dto.request.PortfolioFilteredRequest;
import com.ticketmate.backend.admin.portfolio.application.dto.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioAdminInfo;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioFilteredAdminInfo;
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
import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
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
    Page<PortfolioFilteredAdminInfo> portfolioFilteredAdminInfoPage = portfolioRepositoryCustom.filteredPortfolio(
      request.getUsername(),
      request.getNickname(),
      request.getName(),
      request.getPortfolioStatus(),
      request.toPageable()
    );
    return portfolioFilteredAdminInfoPage.map(portfolioAdminMapper::toPortfolioFilteredAdminResponse);
  }

  /**
   * 포트폴리오 상세조회 로직
   *
   * @param portfolioId 포트폴리오 PK
   */
  @Transactional(readOnly = true)
  public PortfolioAdminResponse getPortfolio(UUID portfolioId) {
    PortfolioAdminInfo portfolioAdminInfo = portfolioRepositoryCustom.findPortfolioAdminInfoByPortfolioId(portfolioId);
    return portfolioAdminMapper.toPortfolioAdminResponse(portfolioAdminInfo);
  }

  /**
   * 포트폴리오 상태 변경 로직
   *
   * @param portfolioId 포트폴리오 PK
   * @param request     변경하려는 포트폴리오 상태
   */
  @Transactional
  public void changePortfolioStatus(UUID portfolioId, PortfolioStatusUpdateRequest request) {
    Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
    Member client = portfolio.getMember();
    handlePortfolioStatus(client, portfolio, request.getPortfolioStatus());
  }

  /**
   * 포트폴리오 상태 변경
   *
   * @param member          회원
   * @param portfolio       포트폴리오
   * @param portfolioStatus 변경하려는 포트폴리오 상태
   */
  private void handlePortfolioStatus(Member member, Portfolio portfolio, PortfolioStatus portfolioStatus) {

    // 변경 전 상태 저장
    PortfolioStatus beforeStatus = portfolio.getPortfolioStatus();

    switch (portfolioStatus) {
      case PENDING_REVIEW -> {
        log.error("PENDING_REVIEW 로의 포트폴리오 상태 변경은 불가능합니다. 현재 포트폴리오 상태: {}", portfolio.getPortfolioStatus());
        throw new CustomException(ErrorCode.PORTFOLIO_STATUS_TRANSITION_ERROR);
      }
      case REVIEWING, REJECTED -> {
        portfolio.transitionPortfolioStatus(portfolioStatus);
        NotificationPayload payload = buildPortfolioNotificationPayload(member, portfolioStatus);
        notificationService.sendToMember(member.getMemberId(), payload);
      }
      case APPROVED -> {
        portfolio.transitionPortfolioStatus(portfolioStatus);
        portfolioService.promoteToAgent(portfolio);
        publisher.publishEvent(new PortfolioHandledEvent(portfolio.getPortfolioId(), portfolioStatus));
        NotificationPayload payload = buildPortfolioNotificationPayload(member, portfolioStatus);
        notificationService.sendToMember(member.getMemberId(), payload);
      }
    }
    log.debug("포트폴리오: {}, 상태 변경 완료: {} -> {}", portfolio.getPortfolioId(), beforeStatus, portfolioStatus);
  }

  /**
   * 포트폴리오 알림 payload 생성
   */
  private NotificationPayload buildPortfolioNotificationPayload(Member member, PortfolioStatus portfolioStatus) {
    PortfolioNotificationType notificationType =
        CommonUtil.stringToEnum(PortfolioNotificationType.class, portfolioStatus.name());
    return notificationType.toPayload(member.getNickname());
  }
}
