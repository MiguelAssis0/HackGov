package com.fiap.hackgov.infra.security.headers;

import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProdHeaderSecurityConfig implements HeaderSecurityConfig {

    @Override
    public void apply(HttpSecurity http) throws Exception {
        http .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .xssProtection(Customizer.withDefaults())
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000))
        );
    }
}
