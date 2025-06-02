package yuhan.pro.chatserver.sharedkernel.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private static final String AUTH_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";

  private final JwtValidator jwtValidator;
  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    String requestId = UUID.randomUUID().toString();
    long startTime = System.currentTimeMillis();

    log.info("Request ID: {} - Starting JWT filter for URI: {}", requestId,
        request.getRequestURI());

    try {
      String jwt = resolveToken(request);

      Claims claims = jwtProvider.parseClaims(jwt);

      if (StringUtils.hasText(jwt) && jwtValidator.validate(jwt)) {

        ChatMemberDetails userDetails = ChatMemberDetails.builder()
            .memberId(claims.get("memberId", Long.class))
            .username(claims.get("username", String.class))
            .nickName(claims.get("nickName", String.class))
            .password("N/A")
            .memberRole(MemberRole.valueOf(claims.get("role", String.class)))
            .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Request ID: {} - Valid JWT. Authentication set for user: {}", requestId,
            userDetails.getMemberId());
      } else {
        log.debug("Request ID: {} - No valid JWT found. Clearing SecurityContext.", requestId);
        SecurityContextHolder.clearContext();
      }

      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      log.info("Request ID: {} - Completed JWT filter for URI: {} in {} ms",
          requestId, request.getRequestURI(), duration);
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTH_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(TOKEN_PREFIX.length());
    }
    return null;
  }
}
