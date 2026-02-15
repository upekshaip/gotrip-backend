package com.gotrip.backend.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppConfig(String dbUrl) {
}
