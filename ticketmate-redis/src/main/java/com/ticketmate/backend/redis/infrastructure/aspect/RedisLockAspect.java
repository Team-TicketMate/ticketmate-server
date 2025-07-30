package com.ticketmate.backend.redis.infrastructure.aspect;

import com.ticketmate.backend.redis.application.annotation.RedisLock;
import com.ticketmate.backend.redis.infrastructure.manager.RedisLockManager;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RedisLockAspect {

  private final RedisLockManager redisLockManager;
  private final ExpressionParser parser = new SpelExpressionParser();
  private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

  @Around("@annotation(redisLockAnnotation)")
  public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLockAnnotation) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Object[] args = joinPoint.getArgs();

    // SpEL 컨텍스트에 메서드 파라미터 바인딩
    StandardEvaluationContext context = new StandardEvaluationContext();
    String[] paramNames = nameDiscoverer.getParameterNames(method);
    if (paramNames != null) {
      for (int i = 0; i < paramNames.length; i++) {
        context.setVariable(paramNames[i], args[i]);
      }
    }

    // annotation.key() 에 담긴 SpEL 평가
    String spel = redisLockAnnotation.key();
    String lockKey = parser.parseExpression(spel).getValue(context, String.class);

    // RedisLockManager 를 통해 Lock 획득 -> 메서드 실행 -> Lock 해제
    return redisLockManager.executeLock(lockKey, () -> {
      try {
        return joinPoint.proceed();
      } catch (Throwable throwable) {
        throw new RuntimeException(throwable);
      }
    });
  }
}
