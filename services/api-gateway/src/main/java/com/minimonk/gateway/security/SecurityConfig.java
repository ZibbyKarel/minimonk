package com.minimonk.gateway.security;

import com.minimonk.security.JwtSupport;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtSupport jwtSupport) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers("/api/auth/**", "/actuator/health").permitAll()
                        .pathMatchers("/api/products/**").hasAnyRole("CUSTOMER", "WAREHOUSE_OPERATOR", "ADMIN")
                        .pathMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyExchange().authenticated())
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
            var token = bearerToken(exchange);
            if (token == null) {
                return chain.filter(exchange);
            }
            try {
                Claims claims = jwtSupport.parse(token);
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                var authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
                var request = exchange.getRequest().mutate()
                        .header("X-Minimonk-User-Id", claims.getSubject())
                        .header("X-Minimonk-Username", claims.get("username", String.class))
                        .header("X-Minimonk-Roles", String.join(",", roles))
                        .build();
                var authenticatedExchange = exchange.mutate().request(request).build();
                return chain.filter(authenticatedExchange)
                        .contextWrite(org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication(authentication));
            } catch (RuntimeException ex) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private String bearerToken(ServerWebExchange exchange) {
        var header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
