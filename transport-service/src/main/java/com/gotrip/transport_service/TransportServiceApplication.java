package com.gotrip.transport_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.gotrip.transport_service", "com.gotrip.common_library"})
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class TransportServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TransportServiceApplication.class, args);
	}
}