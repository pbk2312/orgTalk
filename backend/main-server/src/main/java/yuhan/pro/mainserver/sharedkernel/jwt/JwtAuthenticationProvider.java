package yuhan.pro.mainserver.sharedkernel.jwt;

import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.INVALID_JWT_CLAIMS;
import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;

import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

  private final JwtClaimsProvider claimsProvider;
  private final CustomUserDetailsService customUserDetailsService;

  public Authentication getAuthentication(String accessToken) {
    Claims claims = claimsProvider.getClaims(accessToken);
    UserDetails userDetails = getUserDetailsFromClaims(claims);
    Collection<? extends GrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);

    return new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities);
  }

  private UserDetails getUserDetailsFromClaims(Claims claims) {
    validateClaims(claims);
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(
        claims.get("email").toString());
    if (userDetails == null) {
      throw new CustomException(MEMBER_NOT_FOUND);
    }
    return userDetails;
  }

  private static void validateClaims(Claims claims) {
    if (claims == null) {
      throw new CustomException(INVALID_JWT_CLAIMS);
    }
  }

  private Collection<? extends GrantedAuthority> extractAuthoritiesFromClaims(Claims claims) {
    if (claims == null) {
      throw new CustomException(INVALID_JWT_CLAIMS);
    }

    String role = claims.get("role", String.class);
    if (role == null || role.isBlank()) {
      return Collections.emptyList();
    }

    return List.of(new SimpleGrantedAuthority(role));
  }

  public UserDetails getUserDetailsFromRefreshToken(String refreshToken) {
    Claims claims = claimsProvider.getClaims(refreshToken);
    return getUserDetailsFromClaims(claims);
  }
}
