package com.nantian.erp.salary.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.model.ErpEmpFinanceNumber;
import com.nantian.erp.salary.service.ErpSalarySettingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 薪酬设置controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月23日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("salary/setting")
@Api(value = "薪酬设置")
public class ErpSalarySettingController {
	
	@Autowired
	private ErpSalarySettingService salarySettingService;
	
	@ApiOperation(value = "下载员工财务序号模板", notes = "参数：无参数")
	@RequestMapping(value = "/downloadEmpFinanceNumber", method = RequestMethod.POST)
	public RestResponse downloadEmpFinanceNumber() {
		RestResponse result = salarySettingService.downloadEmpFinanceNumber();
		return result;
	}
	
	@ApiOperation(value = "导入员工财务序号", notes = "参数：员工序号Excel文件")
	@RequestMapping(value = "/importEmpFinanceNumber", method = RequestMethod.POST)
	public RestResponse importEmpFinanceNumber(MultipartFile file,@RequestHeader String token) {
		RestResponse result = salarySettingService.importEmpFinanceNumber(file,token);
		return result;
	}
	
	@ApiOperation(value = "导出员工财务序号", notes = "参数：员工序号Excel文件")
	@RequestMapping(value = "/exportEmpFinanceNumber", method = RequestMethod.POST)
	public RestResponse exportEmpFinanceNumber(@RequestBody List<Map<String,Object>> erpEmpFinanceNumberList) {
		RestResponse result = salarySettingService.exportEmpFinanceNumber(erpEmpFinanceNumberList);
		return result;
	}
	
	@ApiOperation(value = "查询全部员工财务序号", notes = "无参数")
	@RequestMapping(value = "/findEmpFinanceNumber", method = RequestMethod.GET)
	public RestResponse findEmpFinanceNumber(@RequestHeader String token) {
		RestResponse result = salarySettingService.findEmpFinanceNumber(token);
		return result;
	}
	
	@ApiOperation(value = "查询权限内的员工财务序号", notes = "无参数")
	@RequestMapping(value = "/findEmpFinanceNumberByPower", method = RequestMethod.GET)
	public RestResponse findEmpFinanceNumberByPower(@RequestHeader String token) {
		RestResponse result = salarySettingService.findEmpFinanceNumberByPower(token);
		return result;
	}
	
	@ApiOperation(value = "新增员工财务序号", notes = "参数是：财务序号相关参数")
	@RequestMapping(value = "/insertEmpFinanceNumber", method = RequestMethod.POST)
	public RestResponse insertEmpFinanceNumber(@RequestBody ErpEmpFinanceNumber erpEmpFinanceNumber) {
		RestResponse result = salarySettingService.insertEmpFinanceNumber(erpEmpFinanceNumber);
		return result;
	}
	
	@ApiOperation(value = "修改员工财务序号", notes = "参数是：财务序号相关参数")
	@RequestMapping(value = "/updateEmpFinanceNumber", method = RequestMethod.POST)
	public RestResponse updateEmpFinanceNumber(@RequestBody ErpEmpFinanceNumber erpEmpFinanceNumber) {
		RestResponse result = salarySettingService.updateEmpFinanceNumber(erpEmpFinanceNumber);
		return result;
	}
	
	/*--------------------------------------供其他工程调用---------------------------------------------------*/
	@ApiOperation(value = "查询全部财务序号", notes = "无参数")
	@RequestMapping(value = "/findAllFinanceNumber", method = RequestMethod.GET)
	public RestResponse findAllFinanceNumber(@RequestHeader String token) {
		RestResponse result = salarySettingService.findAllFinanceNumber(token);
		return result;
	}
}
