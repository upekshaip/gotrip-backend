package com.gotrip.experience_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
		"com.gotrip.experience_service",
		"com.gotrip.common_library"
})
@EnableJpaAuditing
@EnableDiscoveryClient
@ConfigurationPropertiesScan
public class ExperienceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExperienceServiceApplication.class, args);
	}

}
