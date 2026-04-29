package com.minimonk.gateway.config;

import com.minimonk.observability.TraceContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceGatewayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceparent = TraceContext.ensureTraceparent(exchange.getRequest().getHeaders().getFirst(TraceContext.TRACEPARENT));
        var request = exchange.getRequest().mutate().header(TraceContext.TRACEPARENT, traceparent).build();
        var response = exchange.mutate().request(request).build();
        response.getResponse().getHeaders().set(TraceContext.TRACEPARENT, traceparent);
        return chain.filter(response);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
