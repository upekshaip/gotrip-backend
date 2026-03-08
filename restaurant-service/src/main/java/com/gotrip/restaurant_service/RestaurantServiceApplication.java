//package com.gotrip.restaurant_service;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//
//@SpringBootApplication(scanBasePackages = {"com.gotrip.restaurant_service", "com.gotrip.common_library"})
//@EnableJpaAuditing
//@ConfigurationPropertiesScan
//@EnableDiscoveryClient
//public class RestaurantServiceApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(RestaurantServiceApplication.class, args);
//	}
//
//}

package com.gotrip.restaurant_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(
		scanBasePackages = {
				"com.gotrip.restaurant_service",
				"com.gotrip.common_library.config",
				"com.gotrip.common_library.dto",
				"com.gotrip.common_library.exception",
				"com.gotrip.common_library.service"
		}
)
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class RestaurantServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantServiceApplication.class, args);
	}

}
