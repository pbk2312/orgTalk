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
        
        log.error("OAuth2 authentication failed", exception);
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Query String: {}", request.getQueryString());
        log.error("Exception message: {}", exception.getMessage());
        log.error("Exception type: {}", exception.getClass().getName());
        
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();
            log.error("OAuth2 Error Code: {}", error.getErrorCode());
            log.error("OAuth2 Error Description: {}", error.getDescription());
            log.error("OAuth2 Error URI: {}", error.getUri());
        }
        
        if (exception.getCause() != null) {
            log.error("Exception cause: {}", exception.getCause().getMessage());
            log.error("Cause type: {}", exception.getCause().getClass().getName());
            if (exception.getCause().getCause() != null) {
                log.error("Root cause: {}", exception.getCause().getCause().getMessage());
                log.error("Root cause type: {}", exception.getCause().getCause().getClass().getName());
            }
        }
        
        // 에러를 프론트엔드로 전달
        String errorUrl = frontendRedirectUri.trim() + "?error=" + 
                java.net.URLEncoder.encode(exception.getMessage(), "UTF-8");
        
        log.info("Redirecting to error URL: {}", errorUrl);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}

