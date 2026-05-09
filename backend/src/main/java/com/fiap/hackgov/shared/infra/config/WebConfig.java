package com.fiap.hackgov.shared.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableMethodSecurity
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://82.112.245.100:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH","OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("location", "Location")
                .allowCredentials(true);
    }
}
