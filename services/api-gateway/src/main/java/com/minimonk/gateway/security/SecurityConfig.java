package com.minimonk.gateway.security;

import com.minimonk.security.JwtSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtSupport jwtSupport) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(auth -> auth
                        .anyExchange().permitAll())
                .addFilterAt(jwtWebFilter(jwtSupport), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    JwtSupport jwtSupport(
            @Value("${minimonk.jwt.secret}") String secret,
            @Value("${minimonk.jwt.issuer}") String issuer
    ) {
        return new JwtSupport(secret, issuer);
    }

    private WebFilter jwtWebFilter(JwtSupport jwtSupport) {
        return (exchange, chain) -> {
            if (isTokenOptional(exchange)) {
                return chain.filter(exchange);
            }
            var token = bearerToken(exchange);
            if (token == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            try {
                jwtSupport.parse(token);
                return chain.filter(exchange);
            } catch (RuntimeException ex) {
                log.warn("JWT validation failed: {}", ex.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private boolean isTokenOptional(ServerWebExchange exchange) {
        var path = exchange.getRequest().getPath().pathWithinApplication().value();
        return path.equals("/api/auth/login") || path.equals("/actuator/health");
    }

    private String bearerToken(ServerWebExchange exchange) {
        var header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
