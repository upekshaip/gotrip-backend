package com.gotrip.my_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(scanBasePackages = {
		"com.gotrip.my_gateway",
		"com.gotrip.common_library"
},
exclude = {DataSourceAutoConfiguration.class}
)
@ComponentScan(excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASPECTJ,
		pattern = "com.gotrip.common_library.security.*"
))
@EnableDiscoveryClient
public class MyGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyGatewayApplication.class, args);
	}

}