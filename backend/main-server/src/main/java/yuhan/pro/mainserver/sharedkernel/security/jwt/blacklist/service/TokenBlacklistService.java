package yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.entity.BlacklistedToken;
import yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.repository.BlacklistedTokenRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    @Value("${custom.jwt.duration.refresh}")
    private Long refreshTokenExpiration;

    private final RedisTemplate<String, Object> redisTemplate;
    private final BlacklistedTokenRepository tokenRepo;
    
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String BLACKLISTED_VALUE = "blacklisted";

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
                BLACKLIST_PREFIX + refreshToken,
                BLACKLISTED_VALUE,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );
    }

    @SuppressWarnings("unused")
    private void cacheFallback(String refreshToken, Throwable e) {
        log.warn("Failed to write token blacklist to Redis cache, saved to DB only. Error: {}",
                e.getClass().getSimpleName());
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "readFallback")
    public boolean isBlacklisted(String refreshToken) {
        String key = BLACKLIST_PREFIX + refreshToken;

        Boolean inCache = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(inCache)) {
            return true;
        }
        
        boolean blocked = tokenRepo.existsByTokenAndExpiryAfter(refreshToken, Instant.now());
        if (blocked) {
            Instant expiry = tokenRepo.findExpiryByToken(refreshToken)
                    .orElse(Instant.now().plusMillis(refreshTokenExpiration));
            long ttl = expiry.toEpochMilli() - Instant.now().toEpochMilli();
            try {
                redisTemplate.opsForValue().set(
                        key, BLACKLISTED_VALUE, ttl, TimeUnit.MILLISECONDS
                );
            } catch (Exception ex) {
                log.debug("Failed to restore token blacklist to Redis cache. Error: {}",
                        ex.getClass().getSimpleName());
            }
        }
        return blocked;
    }

    @SuppressWarnings("unused")
    private boolean readFallback(String refreshToken, Throwable e) {
        log.warn("Failed to read token blacklist from Redis, checking DB only. Error: {}",
                e.getClass().getSimpleName());
        return tokenRepo.existsByTokenAndExpiryAfter(refreshToken, Instant.now());
    }
}
