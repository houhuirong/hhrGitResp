/**
 * Copyright (c) 2018, NanTian Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.authentication.bin;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.security.ErpAuthorizationFilter;

/**
 * NT-ERP 【权限模块】  服务启动类
 * <p>
 * 
 * @author caoxb
 * @version 1.0 2018年09月08日 
 * @see
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.nantian.erp" })
@EntityScan(basePackages = "com.nantian.erp.**.entity")
@EnableDiscoveryClient
@MapperScan(basePackages="com.nantian.erp.authentication.data.dao")
public class NTErpAuthenticationApp {

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(NTErpAuthenticationApp.class, args);
	}

	/**
	 * 设置需要过滤权限的请求URL
	 * @author ZhangYuWei
	 * @return
	 */
	@Bean
	public FilterRegistrationBean httpFilterRegistrationBean() {

		FilterRegistrationBean registrationBean = new FilterRegistrationBean();

		ErpAuthorizationFilter authorizeFilter = new ErpAuthorizationFilter();
		registrationBean.setFilter(authorizeFilter);

		// 设置token检查过滤的路径
		List<String> urlPatterns = new ArrayList<String>();
		urlPatterns.add("/authentication/*");
		registrationBean.setUrlPatterns(urlPatterns);

		return registrationBean;
	}

}
