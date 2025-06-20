package yuhan.pro.mainserver.sharedkernel.jwt.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import yuhan.pro.mainserver.sharedkernel.jwt.entity.BlacklistedToken;
import yuhan.pro.mainserver.sharedkernel.jwt.repository.BlacklistedTokenRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

  @Value("${custom.jwt.duration.refresh}")
  private Long refreshTokenExpiration;

  private final RedisTemplate<String, Object> redisTemplate;
  private final BlacklistedTokenRepository tokenRepo;
  private static final String PREFIX = "blacklist:";

  public void blacklist(String refreshToken) {
    BlacklistedToken entity = BlacklistedToken.builder()
        .token(refreshToken)
        .expiry(Instant.now().plusMillis(refreshTokenExpiration))
        .build();
    tokenRepo.save(entity);

    writeCache(refreshToken);
  }

  @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "cacheFallback")
  private void writeCache(String refreshToken) {
    redisTemplate.opsForValue().set(
        PREFIX + refreshToken,
        "blacklisted",
        refreshTokenExpiration,
        TimeUnit.MILLISECONDS
    );
  }

  @SuppressWarnings("unused")
  private void cacheFallback(String refreshToken, Throwable e) {
    log.warn("[cacheFallback] Redis 캐시 설정 실패, DB만 저장됨. token={}, error={}",
        refreshToken, e.getClass().getSimpleName());
  }

  @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "readFallback")
  public boolean isBlacklisted(String refreshToken) {
    String key = PREFIX + refreshToken;

    Boolean inCache = redisTemplate.hasKey(key);
    if (inCache) {
      return true;
    }
    boolean blocked = tokenRepo.existsByTokenAndExpiryAfter(
        refreshToken, Instant.now()
    );
    if (blocked) {
      Instant expiry = tokenRepo.findExpiryByToken(refreshToken)
          .orElse(Instant.now().plusMillis(refreshTokenExpiration));
      long ttl = expiry.toEpochMilli() - Instant.now().toEpochMilli();
      try {
        redisTemplate.opsForValue().set(
            key, "blacklisted", ttl, TimeUnit.MILLISECONDS
        );
      } catch (Exception ex) {
        log.warn("[isBlacklisted->cacheRestore] Redis 재캐시 실패. token={}, error={}",
            refreshToken, ex.getClass().getSimpleName());
      }
    }
    return blocked;
  }

  @SuppressWarnings("unused")
  private boolean readFallback(String refreshToken, Throwable e) {
    log.warn("[readFallback] Redis 조회 실패, DB 확인만 수행. token={}, error={}",
        refreshToken, e.getClass().getSimpleName());
    return tokenRepo.existsByTokenAndExpiryAfter(
        refreshToken, Instant.now()
    );
  }
}
