package com.gotrip.common_library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

public record AppConfig() {
//    these are on seconds
    private static  final int ACCESS_EXPIRATION_IN_MINS = 5;
    private static  final int REFRESH_EXPIRATION_IN_MINS = 60;


    public static final int ACCESS_TOKEN_EXPIRATION = ACCESS_EXPIRATION_IN_MINS * 60;
    public static final int REFRESH_TOKEN_EXPIRATION = REFRESH_EXPIRATION_IN_MINS * 60;
}
