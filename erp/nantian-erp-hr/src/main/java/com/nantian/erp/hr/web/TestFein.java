package com.nantian.erp.hr.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.manager.AuthenticationFeignClient;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("test/feign")
@Api(value = "测试feign")
public class TestFein {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AuthenticationFeignClient authenticationFeignClient;
	
	@GetMapping(value = "/findUserByEmpId")
	@ApiOperation(value = "获取岗位申请信息", notes = "获取岗位申请信息")
	public RestResponse findUserByEmpId(@RequestHeader String token,@RequestParam Integer employeeId){
		return authenticationFeignClient.findUserByEmpId(employeeId);
	}
	
	@GetMapping(value = "/deleteToken")
	@ApiOperation(value = "获取岗位申请信息", notes = "获取岗位申请信息")
	public RestResponse deleteToken(@RequestHeader String token,@RequestParam String tokenDeleting){
		return authenticationFeignClient.deleteToken(tokenDeleting);
	}
	
}