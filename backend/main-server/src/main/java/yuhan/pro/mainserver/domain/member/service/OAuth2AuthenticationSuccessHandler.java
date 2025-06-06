package yuhan.pro.mainserver.domain.member.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;
import yuhan.pro.mainserver.sharedkernel.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.RefreshTokenDto;
import yuhan.pro.mainserver.sharedkernel.util.CookieUtils;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;


  @Value("${custom.app.frontend-redirect-uri}")
  private String frontendRedirectUri;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();

    Authentication oAuth2Authentication = new UsernamePasswordAuthenticationToken(
        memberDetails,
        null,
        authentication.getAuthorities()
    );

    RefreshTokenDto refreshTokenDto = tokenProvider.generateRefreshTokenDto(oAuth2Authentication);

    CookieUtils.addCookie(response, "refreshToken", refreshTokenDto.refreshToken(),
        refreshTokenDto.refreshTokenExpiresIn());

    String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
        .build().toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);

  }
}
