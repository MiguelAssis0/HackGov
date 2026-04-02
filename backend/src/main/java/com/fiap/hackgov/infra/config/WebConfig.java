package com.fiap.hackgov.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://match-hub.vercel.app", "http://82.112.245.100:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH","OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("location", "Location")
                .allowCredentials(true);
    }
}
