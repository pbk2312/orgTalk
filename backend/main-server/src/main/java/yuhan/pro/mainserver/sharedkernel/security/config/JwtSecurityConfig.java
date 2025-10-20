package yuhan.pro.mainserver.sharedkernel.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import yuhan.pro.mainserver.sharedkernel.security.authentication.JwtAuthenticationProvider;
import yuhan.pro.mainserver.sharedkernel.security.filter.JwtFilter;
import yuhan.pro.mainserver.sharedkernel.security.jwt.JwtValidator;

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
