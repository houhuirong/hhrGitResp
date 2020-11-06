package com.nantian.erp.salary.web;

import com.nantian.erp.common.base.util.RestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.model.ErpPayRollFlow;
import com.nantian.erp.salary.data.model.ErpPositiveConfirm;
import com.nantian.erp.salary.service.ErpPayRollFlowService;

@RestController
@RequestMapping("salary/payRollFlow")
@Api(value="入职-待我处理上岗工资流程审批")
public class ErpPayRollFlowController {
	
	@Autowired
	private ErpPayRollFlowService erpPayRollFlowService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@ApiOperation(value="新增上岗工资单流程",notes="参数:map")
	@RequestMapping(value="/insertPayRollFlow",method=RequestMethod.POST)
	public RestResponse insertPayRollFlow(@RequestParam Map<String,Object> map){
		RestResponse result=this.erpPayRollFlowService.insertPayRollFlow(map);
		return result;
	}
	
	@ApiOperation(value = "初始化社保基数的提交时间", notes = "")
	@PostMapping(value = "/initSocailSecMonth")
	public RestResponse initSocailSecMonth(@RequestHeader String token) {
		return erpPayRollFlowService.initSocailSecMonth(token);
	}

	@ApiOperation(value = "按照提交时间查询工资单流程表", notes = "")
	@GetMapping(value = "/queryPayrollSecMonth")
	public RestResponse queryPayrollSecMonth(@RequestHeader String token,@RequestParam String type,@RequestParam String startMonth, @RequestParam String endMonth) {
		return erpPayRollFlowService.queryPayrollSecMonth(token,type,startMonth, endMonth);
	}

	@ApiOperation(value = "修改社保基数的提交时间", notes = "")
	@PostMapping(value = "/updateSocailSecMonth")
	public RestResponse updateSocailSecMonth(@RequestBody ErpPayRollFlow params) {
		return erpPayRollFlowService.updateSocailSecMonth(params);
	}

	@ApiOperation(value = "修改positiveMonth日期", notes = "")
	@PostMapping(value = "/updatePositiveMonth")
	public RestResponse updatePositiveMonth(@RequestBody ErpPayRollFlow params) {
		return erpPayRollFlowService.updatePositiveMonth(params);
	}
	
	@ApiOperation(value = "结束员工流程表", notes = "")
	@GetMapping(value = "/deleteEmployee")
	public RestResponse deleteEmployee(@RequestHeader String token,@RequestParam Integer employeeId) {
		return erpPayRollFlowService.deleteEmployee(token,employeeId);
	}
	
	
	@ApiOperation(value = "新增某月转正名单异常", notes = "")
	@PostMapping(value = "/insertPositiveExcetion")
	public RestResponse insertPositiveExcetion(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		return erpPayRollFlowService.insertPositiveExcetion(token, params);
	}
	
	@ApiOperation(value = "根据时间段查询转正确认信息", notes = "")
	@GetMapping(value = "/queryPositiveConfirm")
	public RestResponse queryPositiveConfirm(@RequestHeader String token,@RequestParam String startMonth, @RequestParam String endMonth) {
		return erpPayRollFlowService.queryPositiveConfirm(token, startMonth, endMonth);
	}

	@ApiOperation(value = "修改待处理的上岗工资单、待处理的转正工资单、调薪申请 当前处理人", notes = "")
	@PostMapping(value = "/updateCurrentPersonIdById")
	public RestResponse updateCurrentPersonIdById(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		try {
			return erpPayRollFlowService.updateCurrentPersonIdById(token, params);
		}catch (Exception e){
			logger.error("updateCurrentPersonIdById修改待处理的上岗工资单、待处理的转正工资单当前处理人 出现异常" + e.getMessage(), e);
			return RestUtils.returnFailure("修改失败 ！");
		}
	}

	@ApiOperation(value = "批量修改待处理的上岗工资单、待处理的转正工资单、调薪申请 当前处理人", notes = "")
	@PostMapping(value = "/batchUpdateCurrentPersonId")
	public RestResponse batchUpdateCurrentPersonId(@RequestBody Map<String,Object> allEmployeeInfoMap) {
		try {
			return erpPayRollFlowService.batchUpdateCurrentPersonId(allEmployeeInfoMap);
		}catch (Exception e){
			logger.error("batchUpdateCurrentPersonId批量修改待处理的上岗工资单、待处理的转正工资单当前处理人 出现异常" + e.getMessage(), e);
			return RestUtils.returnFailure("修改失败 ！");
		}
	}

}
