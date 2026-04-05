package com.fiap.hackgov.infra.security.headers;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface HeaderSecurityConfig {
    void apply(HttpSecurity http) throws Exception;
}