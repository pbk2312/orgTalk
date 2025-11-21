package yuhan.pro.mainserver.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${custom.app.frontend-redirect-uri:http://orgtalk.shop/oauth/callback}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        String requestUri = request.getRequestURI();
        String message = exception.getMessage();

        log.warn("OAuth2 인증 실패 - URI: {}, 메시지: {}", requestUri, message);

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();
            log.warn("OAuth2 Error - 코드: {}, 설명: {}", error.getErrorCode(), error.getDescription());
        }

        String errorUrl = frontendRedirectUri.trim() + "?error=" +
                java.net.URLEncoder.encode(message, "UTF-8");
        log.info("오류 페이지로 리다이렉트: {}", errorUrl);

        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}
