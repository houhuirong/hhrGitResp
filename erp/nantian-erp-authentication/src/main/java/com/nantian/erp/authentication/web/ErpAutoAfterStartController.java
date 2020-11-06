package com.nantian.erp.authentication.web;

import com.nantian.erp.authentication.service.ErpAutoAfterStartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;

/** 
 *
 * Description: 服务启动时，将员工信息同步到Redis中(Controller)
 * @author xujianhao
 * @create 2020-05-16
 */
@Controller
@Order(value=1)
public class ErpAutoAfterStartController implements CommandLineRunner {

	@Autowired
	private ErpAutoAfterStartService erpAutoAfterStartService;

	/**
	 * Description：员工信息（员工id、姓名、性别、一级部门id、二级部门id、状态）、
	 * 部门信息（部门id、部门名、部门经理、上级领导）写入redis，供查询
	 * @author ZhangYuWei
	 * @create 2019-05-16
	 */
	@Override
	public void run(String... args) throws Exception {
		erpAutoAfterStartService.saveUserInfoToRedis();
	}

}
