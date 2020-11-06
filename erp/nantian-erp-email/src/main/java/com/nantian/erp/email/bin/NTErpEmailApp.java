/**
 * Copyright (c) 2018, NanTian Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.email.bin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 
 * Description: 【邮件管理】服务启动类
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月08日      		ZhangYuWei          1.0       
 * </pre>
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
@ComponentScan(basePackages = { "com.nantian.erp" })
@EnableScheduling

public class NTErpEmailApp {

	public static void main(String[] args) {
		SpringApplication.run(NTErpEmailApp.class, args);
	}
	
}
