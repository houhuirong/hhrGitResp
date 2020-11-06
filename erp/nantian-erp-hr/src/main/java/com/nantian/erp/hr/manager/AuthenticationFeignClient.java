package com.nantian.erp.hr.manager;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.config.FeignConfig;

@FeignClient(value="nantian-erp-authentication", path="/nantian-erp", configuration=FeignConfig.class)
public interface AuthenticationFeignClient {
	//测试get方法
	@RequestMapping(value = "/authentication/user/findUserByEmpId", method = RequestMethod.GET)
	RestResponse findUserByEmpId(@RequestParam("empId") Integer employeeId);
	
	//测试post方法
	@RequestMapping(value = "/authentication/redis/deleteToken", method = RequestMethod.POST)
	RestResponse deleteToken(@RequestParam("token") String token);
}
