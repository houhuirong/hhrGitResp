package com.nantian.erp.salary.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.vo.ParamTalkSalaryVo;
import com.nantian.erp.salary.service.ErpToBeHiredService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 功能：NT-salary 招聘-所有待入职
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年09月13日                   caoxiubin         1.0        
 * </pre>
 */
@RestController
@RequestMapping("salary/toBeHired")
@Api(value = "招聘-所有待入职")
public class ErpToBeHiredController {

	@Autowired
	private ErpToBeHiredService erpToBeHiredService;
	
	@ApiOperation(value = "查询 招聘-所有待入职", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllToBeHired", method = RequestMethod.GET)
	public RestResponse findAllToBeHired(HttpServletRequest request) {
		String token=request.getHeader("token");
		return this.erpToBeHiredService.findAllToBeHired(token);
	}
	
	@ApiOperation(value = "招聘-修改面试谈薪", notes = "参数:[ [] ]")
	@RequestMapping(value = "/updateTalkSalary", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public RestResponse updateTalkSalary(@RequestBody ParamTalkSalaryVo paramTalkSalaryVo) {
		return this.erpToBeHiredService.updateTalkSalary(paramTalkSalaryVo);
	}
	
	
	
	
}
