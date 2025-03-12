package com.ticketmate.backend.util.redisson;

import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisLockManager {
    private final RedissonClient redissonClient;

    public <T> T executeLock(String lockKey, Long waitTime, Long leaseTime, LockTask<T> task) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // (1) 락 획득 시도
            //     최대 waitTime 초 동안 락 획득을 대기하고,
            //     락 획득에 성공하면 leaseTime 초 후에 자동으로 만료(해제)되도록 설정.
            if (!lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                // (1)-a) 락 획득 실패(=waitTime 내에 다른 프로세스가 락을 놓지 않음)
                log.error("락 획득 실패 - 다른 요청 처리 중. lockKey: {}", lockKey);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILURE);
            }

            // (2) 락 획득 성공 시, 실제 해야 할 작업(task.run())을 실행
            try {
                log.debug("redis lock 획득 성공 lockKey: {}", lockKey);
                return task.run();
            } catch (Exception e) {
                // (2)-a) 작업 실행 도중 예외 발생 시 로깅 후 재발행
                log.error("작업 실행 중 예외 발생");
                throw new RuntimeException("작업 실행 중 예외 발생", e);
            }

        } catch (InterruptedException e) {
            // (3) 락 획득 대기 중에 스레드가 인터럽트되었을 경우
            Thread.currentThread().interrupt();
            log.error("락 획득 대기 중 인터럽트 발생");
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_INTERRUPT);

        } finally {
            // (4) 락 해제
            //     현재 스레드가 아직도 락을 보유 중이면 `unlock()`
            if (lock.isHeldByCurrentThread()) {
                log.debug("현재 스레드가 락을 가지고 있어 해제합니다.");
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    public interface LockTask<T> {
        T run() throws Exception;
    }
}
