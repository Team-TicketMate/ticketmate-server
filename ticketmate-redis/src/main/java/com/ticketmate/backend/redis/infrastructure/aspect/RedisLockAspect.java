package com.ticketmate.backend.redis.infrastructure.aspect;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.redis.application.annotation.RedisLock;
import com.ticketmate.backend.redis.infrastructure.manager.RedisLockManager;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RedisLockAspect {

  private static final String LOCK_KEY_PREFIX = "lock:";

  private final RedisLockManager redisLockManager;
  private final ExpressionParser expressionParser = new SpelExpressionParser();
  private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

  @Around("@annotation(redisLock)")
  public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
    Object[] args = joinPoint.getArgs();

    // SpEL 컨텍스트에 메서드 파라미터 바인딩
    StandardEvaluationContext context = new StandardEvaluationContext();
    if (parameterNames != null) {
      for (int index = 0; index < parameterNames.length; index++) {
        context.setVariable(parameterNames[index], args[index]);
      }
    }

    // annotation.key() 에 담긴 SpEL 평가
    String keyExpression = redisLock.key();
    if (CommonUtil.nvl(keyExpression, "").isEmpty()) {
      log.error("Redisson Lock Key가 비어있습니다.");
      throw new CustomException(ErrorCode.INVALID_LOCK_KEY);
    }

    Expression expression = expressionParser.parseExpression(keyExpression);
    String evaluatedKey = expression.getValue(context, String.class);
    if (CommonUtil.nvl(evaluatedKey, "").isEmpty()) {
      log.error("Redisson Lock Key Expression이 비어있습니다: {}", keyExpression);
      throw new CustomException(ErrorCode.INVALID_LOCK_KEY);
    }

    String lockKey = LOCK_KEY_PREFIX + evaluatedKey;

    // RedisLockManager 를 통해 Lock 획득 -> 메서드 실행 -> Lock 해제
    return redisLockManager.executeLock(lockKey, redisLock.waitTime(), redisLock.leaseTime(), joinPoint::proceed);
  }
}
