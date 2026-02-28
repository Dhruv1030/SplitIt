package com.splitwise.gateway.config;

import com.splitwise.gateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

        private final AuthenticationFilter authenticationFilter;

        @Value("${services.user.url:lb://user-service}")
        private String userServiceUrl;

        @Value("${services.group.url:lb://group-service}")
        private String groupServiceUrl;

        @Value("${services.expense.url:lb://expense-service}")
        private String expenseServiceUrl;

        @Value("${services.settlement.url:lb://settlement-service}")
        private String settlementServiceUrl;

        @Value("${services.payment.url:lb://payment-service}")
        private String paymentServiceUrl;

        @Value("${services.analytics.url:lb://analytics-service}")
        private String analyticsServiceUrl;

        @Value("${services.notification.url:lb://notification-service}")
        private String notificationServiceUrl;

        public RouteConfig(AuthenticationFilter authenticationFilter) {
                this.authenticationFilter = authenticationFilter;
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // Public routes - No authentication required
                                .route("user-register", r -> r
                                                .path("/api/users/register")
                                                .uri(userServiceUrl))

                                .route("user-login", r -> r
                                                .path("/api/users/login")
                                                .uri(userServiceUrl))

                                // Protected routes - Authentication required
                                .route("user-service-protected", r -> r
                                                .path("/api/users/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(userServiceUrl))

                                .route("group-service-protected", r -> r
                                                .path("/api/groups", "/api/groups/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(groupServiceUrl))

                                .route("expense-service-protected", r -> r
                                                .path("/api/expenses", "/api/expenses/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(expenseServiceUrl))

                                .route("settlement-service-protected", r -> r
                                                .path("/api/settlements", "/api/settlements/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(settlementServiceUrl))

                                .route("payment-service-protected", r -> r
                                                .path("/api/payments", "/api/payments/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(paymentServiceUrl))

                                .route("analytics-service-protected", r -> r
                                                .path("/api/analytics", "/api/analytics/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(analyticsServiceUrl))

                                .route("notification-service-protected", r -> r
                                                .path("/api/notifications", "/api/notifications/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(notificationServiceUrl))

                                .route("activity-service-protected", r -> r
                                                .path("/api/activities", "/api/activities/**")
                                                .filters(f -> f.filter(authenticationFilter
                                                                .apply(new AuthenticationFilter.Config())))
                                                .uri(notificationServiceUrl))

                                .build();
        }
}
