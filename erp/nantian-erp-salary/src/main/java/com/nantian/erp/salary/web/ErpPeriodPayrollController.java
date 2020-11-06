package com.nantian.erp.salary.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.vo.ParamSalaryPeriodVo;
import com.nantian.erp.salary.service.ErpPeriodPayrollService;

/**
 * 功能：NT-salary 试用期-上岗工资单
 * @author caoxb
 * @date 2018年09月13日
 */
@RestController
@RequestMapping("salary/period")
@Api(value = "试用期-上岗工资单")
public class ErpPeriodPayrollController {
	

	@Autowired
	private ErpPeriodPayrollService erpPayrollService;
	@Autowired
	private RedisTemplate redisTemplate; 
	
	@ApiOperation(value = "入职-所有待我处理上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllPayrollForMe", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findAllPayrollForMe(@RequestParam String token) {
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		if (erpUser == null) {
			return RestUtils.returnSuccessWithString("token已过期");
		}		
		List<Map<String, Object>> list = this.erpPayrollService.findAllPayrollForMe(token);
		RestResponse result = RestUtils.returnSuccess(list);
		return result;
	}
	
	@ApiOperation(value = "入职-所有上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllPayroll", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RestResponse findAllPayroll(@RequestParam String token,@RequestParam String startMonth,@RequestParam String endMonth) {
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		if (erpUser == null) {
			return RestUtils.returnSuccessWithString("token已过期");
		}		
		List<Map<String, Object>> list = this.erpPayrollService.findAllPayroll(token,startMonth,endMonth);
		RestResponse result = RestUtils.returnSuccess(list);
		return result;
	}
	
	@ApiOperation(value = "入职-新增上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/insertErpPayroll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public RestResponse insertErpPayroll(@RequestBody ParamSalaryPeriodVo paramSalaryVo,HttpServletRequest request) {
		String token=request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		if (erpUser == null) {
			return RestUtils.returnSuccessWithString("token已过期");
		}
		String str = this.erpPayrollService.insertErpPayroll(paramSalaryVo,token);
		return RestUtils.returnSuccessWithString(str);
	}
	
	@ApiOperation(value = "入职-修改上岗工资单", notes = "参数:[ [] ]")
	@RequestMapping(value = "/updateErpPayroll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public RestResponse updateErpPayroll(@RequestBody ParamSalaryPeriodVo paramSalaryVo,HttpServletRequest request) {
		String token=request.getHeader("token");
		String str = this.erpPayrollService.updateErpPayroll(paramSalaryVo,token);
		return RestUtils.returnSuccessWithString(str);
	}
	
	@ApiOperation(value = "入职-锁定上岗工资单", notes = "参数:[ [] ]")
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
	
	@ApiOperation(value="实习生入职-新增上岗工资单",notes="参数:[[]]")
	@RequestMapping(value="/insertErpTraineeSalary",method=RequestMethod.POST)
	public RestResponse insertErpTraineeSalary(@RequestBody ParamSalaryPeriodVo paramSalaryVo,HttpServletRequest request){
		String token=request.getHeader("token");
		if(!StringUtils.isNotBlank(token)){
			return RestUtils.returnSuccessWithString("未携带token");
		}
		
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		if (erpUser == null) {
			return RestUtils.returnSuccessWithString("token已过期");
		}
		String str = this.erpPayrollService.insertErpTraineeSalary(paramSalaryVo,token);
		return RestUtils.returnSuccessWithString(str);
	}
	
	@ApiOperation(value="实习生入职-修改上岗工资单",notes="参数:[[]]")
	@RequestMapping(value="/updateErpTraineeSalary",method=RequestMethod.POST)
	public RestResponse updateErpTraineeSalary(@RequestBody ParamSalaryPeriodVo paramSalaryVo,HttpServletRequest request){
		String token=request.getHeader("token");
		if(!StringUtils.isNotBlank(token)){
			return RestUtils.returnSuccessWithString("未携带token");
		}
		
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		if (erpUser == null) {
			return RestUtils.returnSuccessWithString("token已过期");
		}
		String str = this.erpPayrollService.updateErpTraineeSalary(paramSalaryVo,token);
		return RestUtils.returnSuccessWithString(str);
	}
	
	@ApiOperation(value = "查询上岗工资单的操作记录", notes = "参数:员工编号")
	@RequestMapping(value = "/findPayrollRecord", method = RequestMethod.GET)
	public RestResponse findPayrollRecord(@RequestParam Integer employeeId) {
		RestResponse result = erpPayrollService.findPayrollRecord(employeeId);
		return result;
	}
	
}
