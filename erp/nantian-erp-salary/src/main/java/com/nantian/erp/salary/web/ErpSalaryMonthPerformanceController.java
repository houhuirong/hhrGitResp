package com.nantian.erp.salary.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.service.ErpSalaryMonthPerformanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;

/** 
 * Description: 功能：NT-salary 月度绩效管理
 * @author caoxiubin
 * @version 1.1
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年09月08日                       caoxiubin          1.0        
 * 2018年12月02日                      ZhangYuWei          1.1        
 * </pre>
 */
@RestController
@RequestMapping("salary/monthPerformance")
@Api(value = "月度绩效")
public class ErpSalaryMonthPerformanceController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ErpSalaryMonthPerformanceService erpMonthPerformanceService;
	
	@ApiOperation(value = "查询当前登录用户权限内的所有一级部门", notes = "参数:[ [] ]")
	@RequestMapping(value = "/findAllFirstDepartmentByPowerParams", method = RequestMethod.GET)
	public RestResponse findAllFirstDepartmentByPowerParams(@RequestHeader String token) {
		RestResponse result = erpMonthPerformanceService.findAllFirstDepartmentByPowerParams(token);
		return result;
	}
	
	@ApiOperation(value = "根据当前登陆用户显示用户相关信息", notes = "参数:[ [当前登陆用户token] ]")
	@RequestMapping(value = "/showTableContent", method = RequestMethod.GET)
	public RestResponse showTableContent(HttpServletRequest request) {
		RestResponse result = erpMonthPerformanceService.showTableContent(request);
		return result;
	}
	
	@ApiOperation(value = "根据一级部门展开月度绩效记录", notes = "参数:[ [一级部门ID、月份] ]")
	@RequestMapping(value = "/findSecondErpMonthPerformance", method = RequestMethod.POST)
	public RestResponse findSecondErpMonthPerformance(@RequestBody Map<String, Object> param,@RequestHeader String token) {
		RestResponse result = erpMonthPerformanceService.findSecondErpMonthPerformance(param,token);
		return result;
	}
	
	@ApiOperation(value = "创建月度绩效", notes = "参数:[ [月度绩效表单信息] ]")
	@RequestMapping(value = "/createErpMonthPerformance", method = RequestMethod.POST)
	public RestResponse createErpMonthPerformance(@RequestBody Map<String, Object> firstDepartmentInfo,@RequestHeader String token) {
		
		try {
			RestResponse result = erpMonthPerformanceService.createErpMonthPerformance(firstDepartmentInfo,token);
			return result;
		}catch(Exception e) {
			
			logger.error("createErpMonthPerformance ERROR:" + e.getMessage(), e);
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("Message", e.getMessage());
			return RestUtils.returnFailure(valueError, "Error");
		}
		
		
	}
	
	@ApiOperation(value = "查询当前登录用户权限内所有一级部门的员工信息和月度绩效", notes = "参数：token")
	@RequestMapping(value = "/findAllErpMonthPerformanceByPowerParams", method = RequestMethod.POST)
	public RestResponse findAllErpMonthPerformanceByPowerParams(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpMonthPerformanceService.findAllErpMonthPerformanceByPowerParams(token, params);
		return result;
	}
	
	@RequestMapping(value = "/exportMonthPerformance", method = RequestMethod.GET)
	@ApiOperation(value = "月度绩效导出", notes = "参数是：月度、token")
	public RestResponse exportMonthPerformance(@RequestHeader String token,@RequestParam String erpMonthNum) {
		RestResponse result = erpMonthPerformanceService.exportMonthPerformance(token,erpMonthNum);
		return result;
	}
	
	@ApiOperation(value = "根据一级部门展开月度绩效记录", notes = "参数:[ [一级部门ID、月份] ]")
	@RequestMapping(value = "/initializeCreateMonthPerformance", method = RequestMethod.POST)
	public RestResponse initializeCreateMonthPerformance(@RequestBody Map<String, Object> param, @RequestHeader String token) {
		RestResponse result = erpMonthPerformanceService.initializeCreateMonthPerformance(param, token);
		return result;
	}
	
	@ApiOperation(value = "查询月度绩效的操作记录", notes = "参数:部门编号")
	@RequestMapping(value = "/findMonthPerformanceRecord", method = RequestMethod.POST)
	public RestResponse findMonthPerformanceRecord(@RequestBody Map<String,Object> param) {
		RestResponse result = erpMonthPerformanceService.findMonthPerformanceRecord(param);
		return result;
	}
	
	@ApiOperation(value = "改变月度绩效的状态（锁定、解锁、归档）", notes = "参数:一级部门ID、月份")
	@PutMapping(value = "/changeMonthPerformanceType")
	public RestResponse changeMonthPerformanceType(@RequestBody Map<String,Object> param, @RequestHeader String token) {
		return erpMonthPerformanceService.changeMonthPerformanceType(param, token);
	}
	
	@ApiOperation(value = "月度左侧机构树及考勤、绩效状态", notes = "参数:部门ID、月份")
	@RequestMapping(value = "/queryDepartMentListAndPerformanceStatus",method = RequestMethod.GET)
	public RestResponse queryDepartMentListAndPerformanceStatus(@RequestParam String month, @RequestHeader String token) {
		return erpMonthPerformanceService.queryDepartMentListAndPerformanceStatus(month, token);
	}
	
	@ApiOperation(value = "锁定指定月份的月度绩效", notes = "参数:Map type=lock  unlock  archive")
	@RequestMapping(value = "/changeCurMonthPerformanceType",method = RequestMethod.POST)
	public RestResponse changeCurMonthPerformanceType(@RequestBody Map<String,Object>params, @RequestHeader String token) {
		try {
			return erpMonthPerformanceService.changeCurMonthPerformanceType(params,token);
		}catch(Exception e) {
			logger.error("changeCurMonthPerformanceType ERROR:" + e.getMessage(), e);
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("message", "changeCurMonthPerformanceType ERROR！"+e.getMessage());
			return RestUtils.returnFailure(valueError, "Error");
		}
	}

	@ApiOperation(value = "经管提交月度绩效", notes = "参数:Map month=2020-02")
	@RequestMapping(value = "/completePerformance",method = RequestMethod.POST)
	public RestResponse completePerformance(@RequestBody Map<String,Object>params, @RequestHeader String token) {
		try {
			return erpMonthPerformanceService.completePerformance(params,token);
		}catch(Exception e) {
			
			logger.error("completePerformance ERROR:" + e.getMessage(), e);
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("Message", e.getMessage());
			return RestUtils.returnFailure(valueError, "Error");
		}
	}

	@ApiOperation(value = "经管导出指定月份的所有月度绩效", notes = "参数：Map month=2020-0")
	@RequestMapping(value = "/exportMonthPerformanceNew", method = RequestMethod.POST)
	public RestResponse exportMonthPerformanceNew(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		try {
			RestResponse result = erpMonthPerformanceService.exportMonthPerformanceNew(token, params);
			return result;
		}catch(Exception e) {
			logger.error("exportMonthPerformanceNew ERROR:" + e.getMessage(), e);
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("message", e.getMessage());
			return RestUtils.returnFailure(valueError, "Error");
		}
	}

	@ApiOperation(value = "修改月度绩效状态", notes = "参数:Map departmentId month")
	@RequestMapping(value = "/updateFirstDepartmentMonthPerformanceStatus",method = RequestMethod.POST)
	public RestResponse updateFirstDepartmentMonthPerformanceStatus(@RequestBody Map<String,Object> params, @RequestHeader String token) {
		RestResponse restResponse = null;
		try {
			restResponse = erpMonthPerformanceService.updateFirstDepartmentMonthPerformanceStatus(params,token);
			return restResponse;
		}catch (Exception e){
			logger.error("updateFirstDepartmentMonthPerformanceStatus ERROR："+e.getMessage(),e);
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("message", e.getMessage());
			return RestUtils.returnFailure(valueError, "Error");
		}
	}

	@ApiOperation(value = "查询月度绩效状态", notes = "参数:一级部门编号,月份")
	@RequestMapping(value = "/findMonthPerformanceStatusByDepartmentIdAndMonth", method = RequestMethod.POST)
	public RestResponse findMonthPerformanceStatusByDepartmentIdAndMonth(@RequestBody Map<String,Object> param) {
		RestResponse result = erpMonthPerformanceService.findMonthPerformanceStatusByDepartmentIdAndMonth(param);
		return result;
	}
}
