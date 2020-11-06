package com.nantian.erp.salary.web;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.service.ErpExportExcelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 员工薪酬-列表导出
 * @author 曹秀斌
 * @date 2018-09-16
 */
@RestController
@RequestMapping("salary/exportExcel")
@Api(value = "员工薪酬-列表导出")
public class ErpExportExcelController {
	
	@Autowired
	private ErpExportExcelService exportExcelService;
	
	@RequestMapping(value = "/exportToBeHiredEmp", method = RequestMethod.GET)
	@ApiOperation(value = "员工薪酬-所有待入职列表导出", notes = "参数:[ [ ] ]")
	public RestResponse exportToBeHiredEmp() {
		
		return this.exportExcelService.exportToBeHiredEmp();
	}
	
	@RequestMapping(value = "/exportPeriodEmp", method = RequestMethod.GET)
	@ApiOperation(value = "员工薪酬-试用期-上岗工资单 员工列表导出", notes = "参数:[ [ ] ]")
	public RestResponse exportPeriodEmp(@RequestParam String token) {
		
		return this.exportExcelService.exportPeriodEmp(token);
	}
	
	@RequestMapping(value = "/exportPositiveEmp", method = RequestMethod.GET)
	@ApiOperation(value = "员工薪酬-转正-上岗工资单 员工列表导出", notes = "参数:[ [ ] ]")
	public RestResponse exportPositiveEmp(HttpServletRequest request,@RequestParam String positiveMonth) {
		String token=request.getHeader("token");
		return this.exportExcelService.exportPositiveEmp(token,positiveMonth);
	}
	
	
}
