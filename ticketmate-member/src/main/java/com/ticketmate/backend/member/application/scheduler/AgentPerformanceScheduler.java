package com.ticketmate.backend.member.application.scheduler;

import com.ticketmate.backend.member.application.service.AgentRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentPerformanceScheduler {
  private final AgentRankingService agentRankingService;

  /**
   * 애플리케이션 시작 시 실행
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initUpdateRanking() {
    try {
      agentRankingService.updateTotalScoreRanking();
    } catch (Exception e) {
      log.error("애플리케이션 시작 시 대리인 랭킹 업데이트에 실패했습니다.", e);
    }
  }

  /**
   * 매일 새벽 4시에 실행
   */
  @Scheduled(cron = "0 0 4 * * *")
  public void runAgentRankingUpdate() {
    try{
      agentRankingService.updateTotalScoreRanking();
    } catch (Exception e) {
      log.error("정기 대리인 랭킹 업데이트에 실패했습니다.", e);
    }
  }
}
