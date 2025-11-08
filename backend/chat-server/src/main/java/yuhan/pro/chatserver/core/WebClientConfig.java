package yuhan.pro.chatserver.core;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${main-server.url:http://main-server:8080}")
    private String mainServerUrl;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        String normalizedUrl = normalizeUrl(mainServerUrl);
        log.info("WebClient baseUrl configured: {} (original: {})", normalizedUrl, mainServerUrl);
        
        return builder.baseUrl(normalizedUrl)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "http://main-server:8080";
        }
        
        try {
            String trimmed = url.trim();
            
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                trimmed = "http://" + trimmed;
            }
            
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme() != null ? uri.getScheme() : "http";
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath();
            
            if (host == null || host.isEmpty()) {
                host = "main-server";
            }
            
            if (port == -1) {
                port = 8080;
            }
            
            if (path == null || path.equals("/")) {
                path = "";
            }
            
            String normalized = scheme + "://" + host + ":" + port + path;
            
            if (normalized.endsWith("/") && normalized.length() > scheme.length() + 3 + host.length() + 6) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            
            return normalized;
        } catch (Exception e) {
            log.warn("Failed to normalize URL: {}, using default", url, e);
            return "http://main-server:8080";
        }
    }
}
