package yuhan.pro.mainserver.sharedkernel.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;
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

    MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();

    String authority = extractAuthories(authentication);
    Set<Long> orgIds = memberDetails.getOrganizationIds();
    String refreshToken = createRefreshToken(memberDetails.getEmail(), memberDetails.getName(),
        memberDetails.getMemberId(),
        authority,
        orgIds
    );
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

  private String createAccessToken(String email, String name, String authorities, Long memberId,
      Set<Long> orgIds) {
    return Jwts.builder()
        .claim("email", email)
        .claim("role", authorities)
        .claim("name", name)
        .claim("memberId", memberId)
        .claim("orgIds", orgIds)
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime() + accessTokenExpiration))
        .signWith(jwtSecretKey, SIG.HS256)
        .compact();
  }

  private String createRefreshToken(String email, String name, Long memberId, String authorities,
      Set<Long> orgIds) {
    return Jwts.builder()
        .claim("email", email)
        .claim("role", authorities)
        .claim("name", name)
        .claim("memberId", memberId)
        .claim("orgIds", orgIds)
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
    String name = payload.get("name", String.class);
    Long memberId = payload.get("memberId", Long.class);

    @SuppressWarnings("unchecked")
    var orgList = (java.util.List<Integer>) payload.get("orgIds", java.util.List.class);
    Set<Long> orgIds = orgList.stream()
        .map(Long::valueOf)
        .collect(Collectors.toSet());

    String accessToken = createAccessToken(email, name, role, memberId, orgIds);
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
