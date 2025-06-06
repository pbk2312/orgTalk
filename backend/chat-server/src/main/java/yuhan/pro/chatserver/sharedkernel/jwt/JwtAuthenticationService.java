package yuhan.pro.chatserver.sharedkernel.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationService {

  private static final String AUTH_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";

  private final JwtValidator jwtValidator;
  private final JwtProvider jwtProvider;

  public Authentication getAuthenticationFromBearer(String bearerTokenValue) {
    if (!StringUtils.hasText(bearerTokenValue) || !bearerTokenValue.startsWith(TOKEN_PREFIX)) {
      return null;
    }

    String token = bearerTokenValue.substring(TOKEN_PREFIX.length());
    if (!jwtValidator.validate(token)) {
      return null;
    }

    Claims claims = jwtProvider.parseClaims(token);

    // orgIds 클레임을 List<Integer> 로 꺼내서 Set<Long> 으로 변환
    @SuppressWarnings("unchecked")
    List<Integer> orgList = claims.get("orgIds", List.class);
    Set<Long> orgIds = (orgList != null)
        ? orgList.stream().map(Long::valueOf).collect(Collectors.toSet())
        : Set.of();

    ChatMemberDetails userDetails = ChatMemberDetails.builder()
        .memberId(claims.get("memberId", Long.class))
        .username(claims.get("email", String.class))
        .nickName(claims.get("name", String.class))
        .password("N/A")
        .memberRole(MemberRole.valueOf(claims.get("role", String.class)))
        .organizationIds(orgIds)  // orgIds 필드 세팅
        .build();

    return new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );
  }

  public Authentication getAuthenticationFromRequest(HttpServletRequest request) {
    String bearerTokenValue = request.getHeader(AUTH_HEADER);
    return getAuthenticationFromBearer(bearerTokenValue);
  }

  public Authentication getAuthenticationFromStompHeader(
      org.springframework.messaging.simp.stomp.StompHeaderAccessor accessor) {
    String bearerTokenValue = accessor.getFirstNativeHeader(AUTH_HEADER);
    return getAuthenticationFromBearer(bearerTokenValue);
  }
}
