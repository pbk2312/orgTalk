package yuhan.pro.mainserver.sharedkernel.jwt;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.TokenDto;
import yuhan.pro.mainserver.sharedkernel.jwt.mapper.JwtMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

  @Value("${custom.jwt.duration.access}")
  private Long accessTokenExpiration;

  @Value("${custom.jwt.duration.refresh}")
  private Long refreshTokenExpiration;

  private final SecretKey jwtSecretKey;


  public TokenDto generateTokenDto(Authentication authentication) {

    String authority = extractAuthories(authentication);

    String accessToken = createAccessToken(authentication.getName(), authority);
    String refreshToken = createRefreshToken(authentication.getName());

    return JwtMapper.toDto(accessToken, refreshToken, accessTokenExpiration.intValue(),
        refreshTokenExpiration.intValue());
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

  private String createRefreshToken(String email) {
    return Jwts.builder()
        .claim("email", email)
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime() + refreshTokenExpiration))
        .signWith(jwtSecretKey, SIG.HS256)
        .compact();
  }
}
