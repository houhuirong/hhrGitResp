package com.nantian.erp.eureka.server;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServiceApp {

	public static void main(String[] args) {
		
		new SpringApplicationBuilder(EurekaServiceApp.class).web(WebApplicationType.SERVLET).run(args);

	}

}
