package com.gotrip.camera_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.gotrip.camera_service", "com.gotrip.common_library"})
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class CameraServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CameraServiceApplication.class, args);
	}

}
