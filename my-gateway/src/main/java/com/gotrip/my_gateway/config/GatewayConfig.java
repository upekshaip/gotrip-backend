package com.gotrip.my_gateway.config;

import com.gotrip.common_library.service.JWTService;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {
    @Bean
    public JWTService jwtService() {
        return new JWTService();
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_route", r -> r.path("/auth/**")
                        .uri("lb://user-service"))


                .route("user_route", r -> r.path("/user/**")
                        .uri("lb://user-service"))
                .build();
    }
}

