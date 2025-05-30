package yuhan.pro.chatserver.sharedkernel.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtClaimsProvider {

  private final SecretKey secretKey;

  public Claims getClaims(String token) {
    try {
      Jws<Claims> claimsJws = Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token);
      return claimsJws.getPayload();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}
