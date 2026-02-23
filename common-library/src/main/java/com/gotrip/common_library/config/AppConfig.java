package com.gotrip.common_library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

public record AppConfig() {
    public static final int ACCESS_TOKEN_EXPIRATION = 60 * 5;    // 5 mins
    public static final int REFRESH_TOKEN_EXPIRATION = 60 * 60; // 1 hour (60 mins)
}
