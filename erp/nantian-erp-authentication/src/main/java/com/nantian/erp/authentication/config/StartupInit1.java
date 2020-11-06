package com.nantian.erp.authentication.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 服务启动执行
 *
 * @author    yumingxian
 * @version   1.0
 * @Create     2018年9月29日	
 */
@Component
@Order(value=2)
public class StartupInit1 implements CommandLineRunner {

	/**
	 * Description：服务启动执行--顺序为：2
	 * @author yumin
	 * @version 1.0
	 * @create  2018年9月29日
	 */
    @Override
    public void run(String... args) throws Exception {
        //System.out.println("---------------------服务启动执行，执行加载数据等操作 11111111--------------------------");
    }

}

