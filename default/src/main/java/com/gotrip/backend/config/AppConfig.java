package com.gotrip.backend.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppConfig(String dbUrl, String jwtSecret) {
    public static final int ACCESS_TOKEN_EXPIRATION = 60 * 5;    // 5 mins
    public static final int REFRESH_TOKEN_EXPIRATION = 60 * 60; // 1 hour (60 mins)
}
