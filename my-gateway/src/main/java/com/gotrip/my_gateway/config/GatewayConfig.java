package com.gotrip.my_gateway.config;

import com.gotrip.common_library.service.JWTService;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;


@Configuration
public class GatewayConfig {
    @Bean
    public JWTService jwtService() {
        return new JWTService();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // allowNextJsFrontend
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        // This source MUST be the reactive version to be accepted by CorsWebFilter
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_route", r -> r.path("/auth/**")
                        .uri("lb://user-service"))

                .route("user_route", r -> r.path("/user/**")
                        .uri("lb://user-service"))

                .route("hotel_service_route", r -> r.path("/hotel-service/**")
                        .uri("lb://hotel-service"))

                .route("transport_service_route", r -> r.path("/transport-service/**")
                        .uri("lb://transport-service"))
          
                .route("hotel_booking_route", r -> r.path("/hotel-booking/**")
                        .uri("lb://hotel-service"))

                .route("experience_route", r -> r.path("/experience/**")
                        .uri("lb://experience-service"))

                .route("restaurant_service_route", r -> r.path("/restaurant-service/**")
                        .uri("lb://restaurant-service"))
                .build();
    }


}

