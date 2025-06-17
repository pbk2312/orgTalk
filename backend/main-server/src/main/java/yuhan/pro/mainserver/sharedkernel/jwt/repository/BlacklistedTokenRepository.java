package yuhan.pro.mainserver.sharedkernel.jwt.repository;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import yuhan.pro.mainserver.sharedkernel.jwt.entity.BlacklistedToken;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

  boolean existsByTokenAndExpiryAfter(String token, Instant now);

  long deleteByExpiryBefore(Instant now);
}
