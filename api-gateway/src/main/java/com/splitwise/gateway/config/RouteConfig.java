package com.splitwise.gateway.config;

import com.splitwise.gateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    private final AuthenticationFilter authenticationFilter;

    public RouteConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Public routes - No authentication required
                .route("user-register", r -> r
                        .path("/api/users/register")
                        .uri("lb://user-service"))

                .route("user-login", r -> r
                        .path("/api/users/login")
                        .uri("lb://user-service"))

                // Protected routes - Authentication required
                .route("user-service-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://user-service"))

                .route("group-service-protected", r -> r
                        .path("/api/groups/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://group-service"))

                .route("expense-service-protected", r -> r
                        .path("/api/expenses/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://expense-service"))

                .route("settlement-service-protected", r -> r
                        .path("/api/settlements/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://settlement-service"))

                .route("payment-service-protected", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://payment-service"))

                .route("analytics-service-protected", r -> r
                        .path("/api/analytics/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://analytics-service"))

                .build();
    }
}
