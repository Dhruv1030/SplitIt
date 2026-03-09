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
                                // =============================================
                                // API v1 routes (versioned) - rewrite /api/v1/ to /api/
                                // =============================================

                                // v1 Public routes
                                .route("v1-user-register", r -> r
                                                .path("/api/v1/users/register")
                                                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(userServiceUrl))

                                .route("v1-user-login", r -> r
                                                .path("/api/v1/users/login")
                                                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(userServiceUrl))

                                .route("v1-user-refresh-token", r -> r
                                                .path("/api/v1/users/refresh-token")
                                                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(userServiceUrl))

                                .route("v1-friend-request-accept", r -> r
                                                .path("/api/v1/users/friend-requests/accept")
                                                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(userServiceUrl))

                                .route("v1-friend-request-decline", r -> r
                                                .path("/api/v1/users/friend-requests/decline")
                                                .filters(f -> f.rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(userServiceUrl))

                                // v1 Protected routes
                                .route("v1-user-service-protected", r -> r
                                                .path("/api/v1/users/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(userServiceUrl))

                                .route("v1-group-service-protected", r -> r
                                                .path("/api/v1/groups", "/api/v1/groups/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(groupServiceUrl))

                                .route("v1-expense-service-protected", r -> r
                                                .path("/api/v1/expenses", "/api/v1/expenses/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(expenseServiceUrl))

                                .route("v1-settlement-service-protected", r -> r
                                                .path("/api/v1/settlements", "/api/v1/settlements/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(settlementServiceUrl))

                                .route("v1-payment-service-protected", r -> r
                                                .path("/api/v1/payments", "/api/v1/payments/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(paymentServiceUrl))

                                .route("v1-analytics-service-protected", r -> r
                                                .path("/api/v1/analytics", "/api/v1/analytics/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(analyticsServiceUrl))

                                .route("v1-notification-service-protected", r -> r
                                                .path("/api/v1/notifications", "/api/v1/notifications/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(notificationServiceUrl))

                                .route("v1-activity-service-protected", r -> r
                                                .path("/api/v1/activities", "/api/v1/activities/**")
                                                .filters(f -> f
                                                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                                                .rewritePath("/api/v1/(?<segment>.*)", "/api/${segment}"))
                                                .uri(notificationServiceUrl))

                                // =============================================
                                // Original routes (backward compatible)
                                // =============================================

                                // Public routes - No authentication required
                                .route("user-register", r -> r
                                                .path("/api/users/register")
                                                .uri(userServiceUrl))

                                .route("user-login", r -> r
                                                .path("/api/users/login")
                                                .uri(userServiceUrl))

                                // Public friend request accept/decline via email token
                                .route("friend-request-accept", r -> r
                                                .path("/api/users/friend-requests/accept")
                                                .uri(userServiceUrl))

                                .route("friend-request-decline", r -> r
                                                .path("/api/users/friend-requests/decline")
                                                .uri(userServiceUrl))

                                .route("user-refresh-token", r -> r
                                                .path("/api/users/refresh-token")
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
