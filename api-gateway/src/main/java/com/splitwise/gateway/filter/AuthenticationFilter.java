package com.splitwise.gateway.filter;

import com.splitwise.gateway.security.JwtTokenValidator;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtTokenValidator jwtTokenValidator;

    public AuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Extract JWT token from Authorization header
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn("Missing Authorization header for: {}", exchange.getRequest().getPath());
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format for: {}", exchange.getRequest().getPath());
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            try {
                // Extract token (remove "Bearer " prefix)
                String token = authHeader.substring(7);

                // Validate token and extract claims
                String userId = jwtTokenValidator.getUserIdFromToken(token);
                String email = jwtTokenValidator.getEmailFromToken(token);
                String roles = jwtTokenValidator.getRolesFromToken(token);

                logger.debug("JWT validated successfully for user: {}", userId);

                // Add user information to request headers for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id", userId)
                                .header("X-User-Email", email)
                                .header("X-User-Roles", roles))
                        .build();

                return chain.filter(modifiedExchange);

            } catch (JwtException e) {
                logger.error("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid or expired token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                logger.error("Authentication error: {}", e.getMessage(), e);
                return onError(exchange, "Authentication failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String errorResponse = String.format(
                "{\"success\":false,\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}",
                httpStatus.getReasonPhrase(), message, httpStatus.value());

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes())));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
