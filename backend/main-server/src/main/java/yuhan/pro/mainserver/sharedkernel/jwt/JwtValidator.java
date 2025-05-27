package yuhan.pro.mainserver.sharedkernel.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtValidator {


  @Value("${custom.jwt.secrets.app-key}")
  private String appKey;


  public boolean validate(String token) {
    try {

      Jws<Claims> claimsJws = Jwts.parser()
          .verifyWith(getSecretKey())
          .build()
          .parseSignedClaims(token);

      return true;
    } catch (ExpiredJwtException e) {
      log.info("JWT 토큰 만료됨: {}", token);
      return false;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 JWT 토큰: {}", token, e);
      return false;
    }

  }

  public boolean isExpired(String token) {
    try {
      Jwts.parser()
          .verifyWith(getSecretKey())
          .build()
          .parseSignedClaims(token);
      return false;
    } catch (ExpiredJwtException e) {
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 JWT 토큰 검사: {}", token, e);
      return false;
    }
  }

  private SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(appKey.getBytes());
  }
}
