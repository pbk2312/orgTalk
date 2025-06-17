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
    tokenRepo.save(
        BlacklistedToken.builder()
            .token(refreshToken)
            .expiry(Instant.now().plusMillis(refreshTokenExpiration))
            .build()
    );
  }

  @SuppressWarnings("unused")
  private void blacklistFallback(String refreshToken, Throwable e) {
    tokenRepo.save(
        BlacklistedToken.builder()
            .token(refreshToken)
            .expiry(Instant.now().plusMillis(refreshTokenExpiration))
            .build()
    );
    log.warn("[blacklistFallback] Redis down, saved to DB only. token={} error={}",
        refreshToken, e.getClass().getSimpleName());
  }

  @CircuitBreaker(
      name = "redisCircuitBreaker",
      fallbackMethod = "isBlacklistedFallback"
  )
  public boolean isBlacklisted(String refreshToken) {
    return redisTemplate.hasKey(PREFIX + refreshToken);
  }

  @SuppressWarnings("unused")
  private boolean isBlacklistedFallback(String refreshToken, Throwable e) {
    boolean blocked = tokenRepo.existsByTokenAndExpiryAfter(
        refreshToken, Instant.now()
    );
    log.warn(
        "[isBlacklistedFallback] Redis down, DB check only. token={} blocked={} error={}",
        refreshToken, blocked, e.getClass().getSimpleName()
    );
    return blocked;
  }
}
