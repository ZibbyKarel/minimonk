package com.minimonk.gateway.config;

import com.minimonk.observability.TraceContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceGatewayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceparent = TraceContext.ensureTraceparent(exchange.getRequest().getHeaders().getFirst(TraceContext.TRACEPARENT));
        var request = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.set(TraceContext.TRACEPARENT, traceparent);
                return headers;
            }
        };
        var tracedExchange = exchange.mutate().request(request).build();
        tracedExchange.getResponse().beforeCommit(() -> {
            tracedExchange.getResponse().getHeaders().set(TraceContext.TRACEPARENT, traceparent);
            return Mono.empty();
        });
        return chain.filter(tracedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
