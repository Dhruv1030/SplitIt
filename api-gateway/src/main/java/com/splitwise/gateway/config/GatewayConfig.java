package com.splitwise.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("lb://user-service"))
                .route("group-service", r -> r
                        .path("/api/groups/**")
                        .uri("lb://group-service"))
                .route("expense-service", r -> r
                        .path("/api/expenses/**")
                        .uri("lb://expense-service"))
                .route("settlement-service", r -> r
                        .path("/api/settlements/**")
                        .uri("lb://settlement-service"))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://payment-service"))
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .uri("lb://analytics-service"))
                .build();
    }
}
