package yuhan.pro.mainserver.sharedkernel.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.AccessTokenResponse;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.RefreshTokenDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

  @Value("${custom.jwt.duration.access}")
  private Long accessTokenExpiration;

  @Value("${custom.jwt.secrets.app-key}")
  private String appKey;

  @Value("${custom.jwt.duration.refresh}")
  private Long refreshTokenExpiration;

  private final SecretKey jwtSecretKey;

  public RefreshTokenDto generateRefreshTokenDto(Authentication authentication) {

    String authority = extractAuthories(authentication);
    String refreshToken = createRefreshToken(authentication.getName(), authority);
    return RefreshTokenDto.builder()
        .refreshToken(refreshToken)
        .refreshTokenExpiresIn(refreshTokenExpiration.intValue())
        .build();
  }

  private String extractAuthories(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));
  }

  private String createAccessToken(String email, String authorities) {
    return Jwts.builder()
        .claim("email", email)
        .claim("role", authorities)
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime() + accessTokenExpiration))
        .signWith(jwtSecretKey, SIG.HS256)
        .compact();
  }

  private String createRefreshToken(String email, String authorities) {
    return Jwts.builder()
        .claim("email", email)
        .claim("role", authorities)
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime() + refreshTokenExpiration))
        .signWith(jwtSecretKey, SIG.HS256)
        .compact();
  }

  public AccessTokenResponse getAccessToken(String token) {
    Claims payload = Jwts.parser()
        .verifyWith(getSecretKey())
        .build()
        .parseSignedClaims(token).getPayload();

    String email = payload.get("email", String.class);
    String role = payload.get("role", String.class);
    String accessToken = createAccessToken(email, role);
    return AccessTokenResponse
        .builder()
        .accessToken(accessToken)
        .accessTokenExpiresIn(accessTokenExpiration.intValue())
        .build();
  }

  private SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(appKey.getBytes());
  }
}
