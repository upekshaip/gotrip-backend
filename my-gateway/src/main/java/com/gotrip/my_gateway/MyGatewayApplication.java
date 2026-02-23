package com.gotrip.my_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.gotrip.my_gateway")
@EnableDiscoveryClient
public class MyGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyGatewayApplication.class, args);
	}

}