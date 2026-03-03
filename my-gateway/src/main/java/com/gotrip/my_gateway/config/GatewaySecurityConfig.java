package com.gotrip.my_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // Note the "Flux" here
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**").permitAll() // Public routes
                        .pathMatchers("/user/**").permitAll()
                        .pathMatchers("/hotel-service/**").permitAll()
                        .anyExchange().authenticated()        // Gateway-level protection
                )
                .build();
    }
}
