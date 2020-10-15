package com.example.servicecenter2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class Servicecenter2Application {

	public static void main(String[] args) {
		SpringApplication.run(Servicecenter2Application.class, args);
	}

}
