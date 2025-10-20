package yuhan.pro.mainserver.core;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import yuhan.pro.mainserver.domain.auth.service.OAuth2AuthenticationSuccessHandler;
import yuhan.pro.mainserver.domain.auth.service.OAuth2Service;
import yuhan.pro.mainserver.sharedkernel.security.handler.JwtAccessDeniedHandler;
import yuhan.pro.mainserver.sharedkernel.security.handler.JwtAuthenticationEntryPoint;
import yuhan.pro.mainserver.sharedkernel.security.authentication.JwtAuthenticationProvider;
import yuhan.pro.mainserver.sharedkernel.security.config.JwtSecurityConfig;
import yuhan.pro.mainserver.sharedkernel.security.jwt.JwtValidator;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtValidator validator;
  private final JwtAuthenticationProvider authenticationProvider;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2Service oAuth2Service;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler accessDeniedHandler;


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .formLogin(form -> form.disable())
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/oauth2/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/api/auth/me").permitAll()
            .requestMatchers("/api/auth/refresh").permitAll()
            .requestMatchers("/api/member/profile-url").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2Service))
        )
        .with(new JwtSecurityConfig(validator, authenticationProvider), c -> {
        });

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:3000"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
