package yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.repository.BlacklistedTokenRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class BlacklistCleanupScheduler {

  private final BlacklistedTokenRepository tokenRepo;

  @Scheduled(cron = "0 0 0 * * *")
  public void removeExpiredTokens() {
    Instant now = Instant.now();
    long deleted = tokenRepo.deleteByExpiryBefore(now);
    log.info("Expired blacklisted tokens removed: {}", deleted);
  }
}
