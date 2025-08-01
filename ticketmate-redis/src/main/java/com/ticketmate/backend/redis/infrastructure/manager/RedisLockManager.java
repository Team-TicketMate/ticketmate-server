package com.ticketmate.backend.redis.infrastructure.manager;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.redis.infrastructure.properties.RedissonProperties;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisLockManager {

  private final RedissonClient redissonClient;
  private final RedissonProperties properties;

  /**
   * Redisson Lock을 획득합니다
   *
   * @param lockKey 락 식별자로 사용할 키
   * @param task    락 획득 후 실행할 작업
   * @param <T>     작업 실행 결과 타입
   */
  public <T> T executeLock(String lockKey, LockTask<T> task) {
    RLock lock = redissonClient.getLock(lockKey);

    try {
      // 락 획득 시도
      if (!lock.tryLock(properties.waitTime(), properties.leaseTime(), TimeUnit.SECONDS)) {
        // 락 획득 실패(=waitTime 내에 다른 프로세스가 락을 놓지 않음)
        log.error("락 획득 실패 - 다른 요청 처리 중. lockKey: {}", lockKey);
        throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILURE);
      }

      // 락 획득 성공 시, 실제 해야 할 작업(task.run())을 실행
      try {
        log.debug("redis lock 획득 성공 lockKey: {}", lockKey);
        return task.run();
      } catch (Exception e) {
        // 작업 실행 도중 예외 발생 시 로깅 후 재발행
        log.error("작업 실행 중 예외 발생");
        throw new RuntimeException("작업 실행 중 예외 발생", e);
      }

    } catch (InterruptedException e) {
      // 락 획득 대기 중에 스레드가 인터럽트되었을 경우
      Thread.currentThread().interrupt();
      log.error("락 획득 대기 중 인터럽트 발생");
      throw new CustomException(ErrorCode.LOCK_ACQUISITION_INTERRUPT);

    } finally {
      // 락 해제 - 현재 스레드가 아직도 락을 보유 중이면 `unlock()`
      if (lock.isHeldByCurrentThread()) {
        log.debug("현재 스레드가 락을 가지고 있어 해제합니다.");
        lock.unlock();
      }
    }
  }

  /**
   * 분산 락 실행 시 사용할 작업 인터페이스
   *
   * @param <T> 작업 실행 결과 타입
   */
  @FunctionalInterface
  public interface LockTask<T> {

    /**
     * 락 획득 후 실행할 작업 로직 구현
     *
     * @return 작업 실행 결과
     */
    T run() throws Exception;
  }
}
