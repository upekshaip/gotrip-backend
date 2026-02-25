package com.gotrip.my_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.common_library.service.JWTService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayJwtFilter implements GlobalFilter, Ordered {

    private final JWTService jwtService;
    private final ObjectMapper objectMapper;

    public GatewayJwtFilter(JWTService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                message,
                status.value(),
                System.currentTimeMillis()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        } catch (Exception ex) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // 1. Skip validation for Auth endpoints (Signup/Login)
        if ((path.equals("/auth/login") && method.equals("POST")) ||
                (path.equals("/auth/signup") && method.equals("POST"))) {
            System.out.println("Bypassing security for public route: " + method + " " + path);
            return chain.filter(exchange);
        }

        // 2. Check for Authorization Header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid JWT Token");
        }

        String token = authHeader.substring(7);
        try {
            // 3. Extract the "user" claim (JSON string) using your JWTService
            String userJson = jwtService.extractClaim(token, claims -> claims.get("user", String.class));
            String email = jwtService.extractEmail(token);

            if (userJson == null || email == null) {
                throw new RuntimeException("Invalid token payload");
            }

            System.out.println("request from: " + email);

            // 4. Mutate the request to add the 'x-user' header
            // This header will now be available to all downstream services (User-Service, Trip-Service, etc.)
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("x-user", userJson)
                    .header("x-user-email", email)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "Invalid or Expired Token", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public int getOrder() {
        return -1; // Execute early in the filter chain
    }
}