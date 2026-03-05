package com.splitwise.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Manual CORS WebFilter that runs BEFORE DispatcherHandler.
 * After setting CORS response headers it strips the Origin header
 * from the request so that downstream CorsProcessors (in
 * RoutePredicateHandlerMapping and downstream micro-services)
 * never see a cross-origin request and skip their own checks.
 */
@Configuration
public class CorsConfig {

        private static final Set<String> ALLOWED_ORIGINS = Set.of(
                        "http://localhost:3000",
                        "http://localhost:4200",
                        "http://localhost:5173",
                        "http://localhost:8100",
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:4200",
                        "http://127.0.0.1:5173",
                        "https://delightful-mud-0f191e10f.2.azurestaticapps.net");

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public WebFilter corsFilter() {
                return (ServerWebExchange exchange, WebFilterChain chain) -> {
                        ServerHttpRequest request = exchange.getRequest();
                        String origin = request.getHeaders().getOrigin();

                        if (origin == null || !ALLOWED_ORIGINS.contains(origin)) {
                                // Not a CORS request, or origin not allowed → pass through
                                return chain.filter(exchange);
                        }

                        ServerHttpResponse response = exchange.getResponse();

                        // Set CORS response headers
                        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

                        // Handle preflight
                        if (request.getMethod() == HttpMethod.OPTIONS) {
                                response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                                                "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
                                response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                                                "Authorization, Content-Type, X-Requested-With, Accept, Origin, X-User-Id");
                                response.setStatusCode(HttpStatus.OK);
                                return response.setComplete();
                        }

                        // Strip Origin so downstream CorsProcessors see a same-origin request
                        ServerHttpRequest mutatedRequest = request.mutate()
                                        .headers(h -> h.remove(HttpHeaders.ORIGIN))
                                        .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                };
        }
}