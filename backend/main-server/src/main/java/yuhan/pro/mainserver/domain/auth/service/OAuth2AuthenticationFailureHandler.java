package yuhan.pro.mainserver.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
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
        
        log.error("OAuth2 authentication failed", exception);
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Query String: {}", request.getQueryString());
        log.error("Exception message: {}", exception.getMessage());
        log.error("Exception cause: {}", exception.getCause());
        
        // 에러를 프론트엔드로 전달
        String errorUrl = frontendRedirectUri.trim() + "?error=" + 
                java.net.URLEncoder.encode(exception.getMessage(), "UTF-8");
        
        log.info("Redirecting to error URL: {}", errorUrl);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}

