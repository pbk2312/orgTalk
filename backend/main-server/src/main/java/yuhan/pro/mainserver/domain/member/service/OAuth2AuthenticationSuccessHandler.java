package yuhan.pro.mainserver.domain.member.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;
import yuhan.pro.mainserver.sharedkernel.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.TokenDto;
import yuhan.pro.mainserver.sharedkernel.util.CookieUtils;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();

    Authentication oAuth2Authentication = new UsernamePasswordAuthenticationToken(
        memberDetails.getEmail(),
        null,
        authentication.getAuthorities()
    );

    TokenDto tokenDto = tokenProvider.generateTokenDto(oAuth2Authentication);

    CookieUtils.addCookie(response, "accessToken", tokenDto.accessToken(),
        tokenDto.accessTokenExpiresIn());
    CookieUtils.addCookie(response, "refreshToken", tokenDto.refreshToken(),
        tokenDto.refreshTokenExpiresIn());

    String targetUrl = "/";

    getRedirectStrategy().sendRedirect(request, response, targetUrl);

  }
}
