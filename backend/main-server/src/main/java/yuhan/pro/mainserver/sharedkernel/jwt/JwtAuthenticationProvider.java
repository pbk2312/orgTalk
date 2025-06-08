package yuhan.pro.mainserver.sharedkernel.jwt;

import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.INVALID_JWT_CLAIMS;

import io.jsonwebtoken.Claims;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

  private final JwtClaimsProvider claimsProvider;
  // loadUserByUsername 호출을 없애기 때문에 CustomUserDetailsService는 주입하지 않습니다.
  // private final CustomUserDetailsService customUserDetailsService;

  public Authentication getAuthentication(String accessToken) {
    Claims claims = claimsProvider.getClaims(accessToken);
    UserDetails userDetails = getUserDetailsFromClaims(claims);
    Collection<? extends GrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);

    return new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities);
  }

  private UserDetails getUserDetailsFromClaims(Claims claims) {
    validateClaims(claims);

    // (1) 이메일 검증
    String email = claims.get("email", String.class);
    if (email == null || email.isBlank()) {
      throw new CustomException(INVALID_JWT_CLAIMS);
    }

    // (2) 토큰에 들어 있는 값들을 꺼낸다.
    String name = claims.get("name", String.class);
    Long memberId = claims.get("memberId", Long.class);

    String roleStr = claims.get("role", String.class);
    if (roleStr == null || roleStr.isBlank()) {
      throw new CustomException(INVALID_JWT_CLAIMS);
    }

    @SuppressWarnings("unchecked")
    List<Integer> orgIdsFromToken = claims.get("orgIds", List.class);
    Set<Long> organizationIds = (orgIdsFromToken == null)
        ? Set.of()
        : orgIdsFromToken.stream()
            .map(Long::valueOf)
            .collect(Collectors.toSet());

    // (3) 토큰 클레임 기반으로 직접 CustomUserDetails 생성
    CustomUserDetails userDetails = CustomUserDetails.builder()
        .memberId(memberId)
        .username(email)
        .nickName(name)
        .memberRole(MemberRole.valueOf(roleStr))
        .organizationIds(organizationIds)
        .password("PASSWORD")
        .build();

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
      return List.of();
    }

    List<GrantedAuthority> authorities = new ArrayList<>();
    // 예: "MEMBER" → ROLE_MEMBER 형태로 만든다.
    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

    @SuppressWarnings("unchecked")
    List<Integer> orgIdsFromToken = claims.get("orgIds", List.class);
    if (orgIdsFromToken != null) {
      orgIdsFromToken.stream()
          .map(id -> "ROLE_ORG_" + id)      // “조직별 권한(ROLE_ORG_5 등)”을 추가하고 싶다면
          .map(SimpleGrantedAuthority::new)
          .forEach(authorities::add);
    }

    return authorities;
  }
}
