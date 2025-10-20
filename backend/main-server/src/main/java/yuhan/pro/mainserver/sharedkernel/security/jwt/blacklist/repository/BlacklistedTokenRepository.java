package yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.entity.BlacklistedToken;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

  boolean existsByTokenAndExpiryAfter(String token, Instant now);

  long deleteByExpiryBefore(Instant now);

  @Query("SELECT b.expiry FROM BlacklistedToken b WHERE b.token = :token")
  Optional<Instant> findExpiryByToken(@Param("token") String token);
}
