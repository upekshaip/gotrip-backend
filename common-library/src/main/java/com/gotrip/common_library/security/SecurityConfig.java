package com.gotrip.common_library.security;

import com.gotrip.common_library.filter.CommonIdentityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CommonIdentityFilter commonIdentityFilter;

    public SecurityConfig(CommonIdentityFilter commonIdentityFilter) {
        this.commonIdentityFilter = commonIdentityFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Login/Signup
                        .anyRequest().authenticated()           // Everything else needs the header
                )
                // Use the new header-based identity filter
                .addFilterBefore(commonIdentityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}