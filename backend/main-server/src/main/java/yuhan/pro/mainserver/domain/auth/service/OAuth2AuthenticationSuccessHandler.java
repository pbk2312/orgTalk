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
import yuhan.pro.mainserver.sharedkernel.common.util.CookieUtils;
import yuhan.pro.mainserver.sharedkernel.security.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.security.jwt.dto.TokenDto;

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
        try {
            MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
            log.info("OAuth2 인증 성공 - memberId: {}", memberDetails.getMemberId());

            Authentication oAuth2Authentication = new UsernamePasswordAuthenticationToken(
                    memberDetails,
                    null,
                    authentication.getAuthorities()
            );

            TokenDto tokenDto = tokenProvider.generateTokenDto(oAuth2Authentication);

            CookieUtils.addCookie(response, REFRESH_TOKEN_COOKIE, tokenDto.refreshToken(),
                    tokenDto.refreshTokenExpiresIn());

            String normalizedRedirectUri = frontendRedirectUri.trim();
            String targetUrl = UriComponentsBuilder.fromUriString(normalizedRedirectUri)
                    .queryParam("accessToken", tokenDto.accessToken())
                    .queryParam("expiresIn", tokenDto.accessTokenExpiresIn())
                    .build().toUriString();

            log.info("프론트엔드로 리다이렉트: {}", targetUrl);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생: {}", e.getMessage());
            throw new IOException("OAuth2 인증 처리 실패", e);
        }
    }
}
