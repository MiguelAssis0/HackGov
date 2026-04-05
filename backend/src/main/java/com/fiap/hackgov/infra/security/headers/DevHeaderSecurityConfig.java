package com.fiap.hackgov.infra.security.headers;

import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevHeaderSecurityConfig implements HeaderSecurityConfig {
    @Override
    public void apply(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https:; " +
                                        "style-src 'self' 'unsafe-inline' https:; " +
                                        "img-src 'self' data:; " +
                                        "frame-src 'self' http://localhost:*;"
                        ))
                )
                .csrf(csrf -> csrf.disable());
    }
}
