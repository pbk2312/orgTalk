package yuhan.pro.chatserver.sharedkernel.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final JwtAuthenticationService jwtAuthService;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    // "/ws-stomp/**" 경로는 필터링 제외
    return pathMatcher.match("/ws-stomp/**", uri);
  }

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
      // --- 리팩터링된 부분: JwtAuthenticationService 사용 ---
      Authentication authentication = jwtAuthService.getAuthenticationFromRequest(request);
      if (authentication != null) {
        log.info("Request ID: {} - Valid JWT found. User: {}", requestId,
            ((ChatMemberDetails) authentication.getPrincipal()).getUsername());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
}
