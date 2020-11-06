package com.nantian.erp.salary.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.vo.ParamSalaryPositiveVo;
import com.nantian.erp.salary.service.AutomaticPositionScheduler;
import com.nantian.erp.salary.service.ErpPeriodPayrollService;
import com.nantian.erp.salary.service.ErpPositivePayrollService;

/**
 * 功能：NT-salary 转正-上岗工资单
 * @author caoxb
 * @date 2018年09月14日
 */
@RestController
@RequestMapping("salary/positive")
@Api(value = "转正-上岗工资单")
public class ErpPositivePayrollController {
	
	@Autowired
	private ErpPositivePayrollService erpPositivePayrollService;
	@Autowired
	private ErpPeriodPayrollService erpPayrollService;
	@Autowired
	private RedisTemplate redisTemplate; 
	
	@Autowired
	private AutomaticPositionScheduler automaticPositionScheduler;
	
	@ApiOperation(value = "转正-所有待我处理上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllPayrollForMe", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findAllPayrollForMe(@RequestParam String token) {		 
		List<Map<String, Object>> list = this.erpPositivePayrollService.findAllPayrollForMe(token);
		RestResponse result = RestUtils.returnSuccess(list);
		return result;
	}
	
	@ApiOperation(value = "转正-所有上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllPayroll", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findAllPayroll(@RequestParam String token,@RequestParam String startMonth,@RequestParam String endMonth) {
		List<Map<String, Object>> list = this.erpPositivePayrollService.findAllPayroll(token,startMonth,endMonth);
		RestResponse result = RestUtils.returnSuccess(list);
		return result;
	}
	
	@ApiOperation(value = "转正-新增上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/insertErpPayroll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public RestResponse insertErpPayroll(@RequestBody ParamSalaryPositiveVo paramSalaryPositiveVo,HttpServletRequest request) {
		String token=request.getHeader("token");
		String str = this.erpPositivePayrollService.insertErpPayroll(paramSalaryPositiveVo,token);
		return RestUtils.returnSuccessWithString(str);
	}
	
	@ApiOperation(value = "转正-修改上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/updateErpPayroll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public RestResponse updateErpPayroll(@RequestBody ParamSalaryPositiveVo paramSalaryPositiveVo,HttpServletRequest request) {
		String token=request.getHeader("token");
		return this.erpPositivePayrollService.updateErpPayroll(paramSalaryPositiveVo,token);
	}
	
	@ApiOperation(value = "转正-锁定转正工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/updateErpPayRollFlow", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public RestResponse updateErpPayRollFlow(@RequestBody Map<String,Object> map,HttpServletRequest request){
		String token=request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		if (erpUser == null) {
			return RestUtils.returnSuccessWithString("token已过期");
		}
		String str=this.erpPayrollService.updateErpPayRollFlow(map,token);
		return RestUtils.returnSuccessWithString(str);
				
	}
	
	@ApiOperation(value = "查询转正工资单的操作记录", notes = "参数:员工编号")
	@RequestMapping(value = "/findPayrollRecord", method = RequestMethod.GET)
	public RestResponse findPayrollRecord(@RequestParam Integer employeeId) {
		RestResponse result = erpPositivePayrollService.findPayrollRecord(employeeId);
		return result;
	}
	
	//@Scheduled(cron = "0/10 * * * * ?")
	@Scheduled(cron = "0 0 2 4 * ?")
	public void automaticLockScheduler() {
		erpPositivePayrollService.automaticLockScheduler();
	}

	@ApiOperation(value = "测试自动转正", notes = "无")
	@RequestMapping(value = "/automaticLockScheduler2", method = RequestMethod.GET)
	public void automaticLockScheduler2() {
		erpPositivePayrollService.automaticLockScheduler();
		return;
	}
	
	@ApiOperation(value = "测试-每天自动转正-手工操作", notes = "无")
	@RequestMapping(value = "/automaticPosition", method = RequestMethod.GET)
	public void automaticPosition() {
		automaticPositionScheduler.automaticPosition();
		return;
	}
}
