package yuhan.pro.mainserver.sharedkernel.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class JwtSecurityConfig extends AbstractHttpConfigurer<JwtSecurityConfig, HttpSecurity> {

  private final JwtValidator validator;
  private final JwtAuthenticationProvider authenticationProvider;

  @Override
  public void configure(HttpSecurity http) {
    JwtFilter jwtFilter = new JwtFilter(validator, authenticationProvider);
    http
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
  }
}
