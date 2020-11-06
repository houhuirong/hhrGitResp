package com.nantian.erp.hr.web;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpEmpDepartmentInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 功能：用于其他服务关于员工-部门信息的接口调用
 * @author caoxb
 * @date 2018年09月08日
 *
 */
@RestController
@RequestMapping("erp/empDepartment/Info")
@Api(value = "员工-部门信息  功能-用于其他服务接口调用")
public class ErpEmpDepartmentInfoController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ErpEmpDepartmentInfoService erpMonthPerformanceService;
	
	@ApiOperation(value = "根据offerID查询岗位信息", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findPostInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findPostInfo(@RequestParam Integer offerId) {
		logger.info("根据offerID查询面试记录  参数：" + null);
		Map<String, Object> resultMap = 
				this.erpMonthPerformanceService.findPostInfo(offerId);
		return RestUtils.returnSuccess(resultMap);
	}
	
	@ApiOperation(value = "试用期-员工及其部门信息", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllHasHiredInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findAllHasHiredInfo(@RequestParam String userName) {
		logger.info("根据简历ID查询面试记录  参数：" + null);
		List<Map<String, Object>> resultMap = 
				this.erpMonthPerformanceService.findAllHasHiredInfo(userName);
		return RestUtils.returnSuccess(resultMap);
	}
	
	@ApiOperation(value = "转正-员工及其部门信息", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllPositiveInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findAllPositiveInfo(@RequestParam String userName) {
		logger.info("根据简历ID查询面试记录  参数：" + null);
		List<Map<String, Object>> resultMap = 
				this.erpMonthPerformanceService.findAllPositiveInfo(userName);
		return RestUtils.returnSuccess(resultMap);
	}
	
	@ApiOperation(value = "根据员工ID查询员工及其部门-岗位信息", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findEmpInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findEmpInfo(@RequestParam Integer employeeId) {
		logger.info("根据员工ID查询员工及其部门-岗位信息  参数：" + null);
		Map<String, Object> resultMap = 
				this.erpMonthPerformanceService.findEmpInfo(employeeId);
		return RestUtils.returnSuccess(resultMap);
	}
	
	
	
}
