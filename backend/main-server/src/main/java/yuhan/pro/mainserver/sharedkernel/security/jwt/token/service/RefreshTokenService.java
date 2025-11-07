package yuhan.pro.mainserver.sharedkernel.security.jwt.token.service;

import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.REFRESH_TOKEN_PREFIX;

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
public class RefreshTokenService {

    @Value("${custom.jwt.duration.refresh}")
    private Long refreshTokenExpiration;

    private final RedisTemplate<String, Object> redisTemplate;

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "saveTokenFallback")
    public void saveRefreshToken(Long memberId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );
        log.debug("Refresh token saved to Redis for memberId: {}", memberId);
    }

    @SuppressWarnings("unused")
    private void saveTokenFallback(Long memberId, String refreshToken, Throwable e) {
        log.warn("Failed to save refresh token to Redis for memberId: {}, error: {}",
                memberId, e.getClass().getSimpleName());
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "getTokenFallback")
    public String getRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    @SuppressWarnings("unused")
    private String getTokenFallback(Long memberId, Throwable e) {
        log.warn("Failed to get refresh token from Redis for memberId: {}, error: {}",
                memberId, e.getClass().getSimpleName());
        return null;
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "existsTokenFallback")
    public boolean existsRefreshToken(Long memberId, String refreshToken) {
        String storedToken = getRefreshToken(memberId);
        boolean exists = refreshToken.equals(storedToken);
        log.debug("Refresh token exists check for memberId: {}, result: {}", memberId, exists);
        return exists;
    }

    @SuppressWarnings("unused")
    private boolean existsTokenFallback(Long memberId, String refreshToken, Throwable e) {
        log.warn("Failed to check refresh token existence in Redis for memberId: {}, error: {}",
                memberId, e.getClass().getSimpleName());
        return true;
    }

    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "deleteTokenFallback")
    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.delete(key);
        log.debug("Refresh token deleted from Redis for memberId: {}", memberId);
    }

    @SuppressWarnings("unused")
    private void deleteTokenFallback(Long memberId, Throwable e) {
        log.warn("Failed to delete refresh token from Redis for memberId: {}, error: {}",
                memberId, e.getClass().getSimpleName());
    }
}

