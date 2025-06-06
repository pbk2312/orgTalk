package yuhan.pro.mainserver.sharedkernel.jwt;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

  @Value("${custom.jwt.duration.refresh}")
  private Long refreshTokenExpiration;

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PREFIX = "blacklist:";

  @CircuitBreaker(
      name = "redisCircuitBreaker",
      fallbackMethod = "blacklistFallback"
  )
  public void blacklist(String refreshToken) {
    redisTemplate.opsForValue().set(
        PREFIX + refreshToken,
        "blacklisted",
        refreshTokenExpiration,
        TimeUnit.MILLISECONDS
    );
  }

  @CircuitBreaker(
      name = "redisCircuitBreaker",
      fallbackMethod = "isBlacklistedFallback"
  )
  public boolean isBlacklisted(String refreshToken) {
    return redisTemplate.hasKey(PREFIX + refreshToken);
  }

  @SuppressWarnings("unused")
  private void blacklistFallback(String refreshToken, Throwable e) {
    log.error("[CircuitBreaker][blacklistFallback] Redis 연결 실패. 토큰 블랙리스트 등록 불가. token={}, error={}",
        refreshToken, e.getClass().getSimpleName());
  }

  @SuppressWarnings("unused")
  private boolean isBlacklistedFallback(String refreshToken, Throwable e) {
    log.error(
        "[CircuitBreaker][isBlacklistedFallback] Redis 연결 실패. 모든 토큰을 차단 처리합니다. token={}, error={}",
        refreshToken, e.getClass().getSimpleName());
    return true;
  }
}
