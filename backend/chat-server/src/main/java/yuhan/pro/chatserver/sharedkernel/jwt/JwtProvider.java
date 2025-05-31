package yuhan.pro.chatserver.sharedkernel.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  @Value("${custom.jwt.secrets.app-key}")
  private String appKey;

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSecretKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(appKey.getBytes());
  }
}
