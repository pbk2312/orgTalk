package yuhan.pro.mainserver.domain.auth.service;

import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.REFRESH_TOKEN_COOKIE;

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
import yuhan.pro.mainserver.sharedkernel.security.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.security.jwt.dto.TokenDto;
import yuhan.pro.mainserver.sharedkernel.common.util.CookieUtils;

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
        log.info("OAuth2 authentication successful for memberId: {}", memberDetails.getMemberId());

        Authentication oAuth2Authentication = new UsernamePasswordAuthenticationToken(
                memberDetails,
                null,
                authentication.getAuthorities()
        );

        TokenDto tokenDto = tokenProvider.generateTokenDto(oAuth2Authentication);

        CookieUtils.addCookie(response, REFRESH_TOKEN_COOKIE, tokenDto.refreshToken(),
                tokenDto.refreshTokenExpiresIn());

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("accessToken", tokenDto.accessToken())
                .queryParam("expiresIn", tokenDto.accessTokenExpiresIn())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}




