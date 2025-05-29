package yuhan.pro.mainserver.sharedkernel.jwt;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {


  @Value("${custom.jwt.duration.refresh}")
  private Long refreshTokenExpiration;

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PREFIX = "blacklist:";

  public void blacklist(String refreshToken) {
    redisTemplate.opsForValue().set(
        PREFIX + refreshToken,
        "blacklisted",
        refreshTokenExpiration,
        TimeUnit.MILLISECONDS
    );
  }

  public boolean isBlacklisted(String refreshToken) {
    return redisTemplate.hasKey(PREFIX + refreshToken);
  }
}
