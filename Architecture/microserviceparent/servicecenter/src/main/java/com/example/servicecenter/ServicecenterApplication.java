package com.example.servicecenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class ServicecenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicecenterApplication.class, args);
	}

}
