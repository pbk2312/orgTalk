package yuhan.pro.chatserver.sharedkernel.infra.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
public class MessageProcessingMonitor {

    @Around("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
    public Object monitorMessageMapping(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 100) {
                log.warn("메시지 처리 지연: method={}, duration={}ms", methodName, duration);
            } else {
                log.debug("메시지 처리 완료: method={}, duration={}ms", methodName, duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("메시지 처리 실패: method={}, duration={}ms, error={}",
                    methodName, duration, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* yuhan.pro.chatserver.sharedkernel.infra.redis.RedisPubSubService.publishChatMessage(..))")
    public Object monitorRedisPublish(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 50) {
                log.warn("Redis 발행 지연: duration={}ms", duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Redis 발행 실패: duration={}ms, error={}", duration, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* yuhan.pro.chatserver.domain.service.ChatService.saveChat*(..))")
    public Object monitorChatSave(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 200) {
                log.warn("채팅 저장 지연: method={}, duration={}ms", methodName, duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("채팅 저장 실패: method={}, duration={}ms, error={}",
                    methodName, duration, e.getMessage());
            throw e;
        }
    }
}

