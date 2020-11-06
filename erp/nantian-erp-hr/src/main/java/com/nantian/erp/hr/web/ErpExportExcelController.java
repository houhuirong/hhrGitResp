package com.nantian.erp.hr.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpExportExcelService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/erp/exportExcel")
@Api(value = "人力资源-报表导出")
public class ErpExportExcelController {
	
	@Autowired
	private ErpExportExcelService exportExcelService;
	
	/*@RequestMapping(value = "/exportOffer/{isValid}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "有效/归档offer-导出", notes = "参数:isValid为1表示“有效”，为2表示“失效”")
	public String exportOffer(@PathVariable Integer isValid) {
		return this.exportExcelService.exportOffer(isValid);
	}*/
	
	@RequestMapping(value = "/exportOffer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "有效/归档offer-导出", notes = "参数:用户选中的offer数组")
	public String exportOffer(@RequestBody List<Integer> offerIds) {
		return this.exportExcelService.exportOffer(offerIds);
	}
	
	@RequestMapping(value = "/exportInterviewing/{flag}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "所有进行中的面试/待我面试-导出", notes = "参数:flag为1表示“待我处理”，为2表示“所有”")
	public String exportInterviewing(@PathVariable Integer flag,@RequestHeader String token) {
		return this.exportExcelService.exportInterviewing(flag,token);
	}
	
	@RequestMapping(value = "/exportPost/{isClosed}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "发布中/已完成的岗位-导出", notes = "参数:isClosed为1表示“已完成”，为2表示“发布中”")
	public String exportPost(@PathVariable Integer isClosed) {
		return this.exportExcelService.exportPost(isClosed);
	}
	
	@RequestMapping(value = "/exportResume/{isValid}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "有效简历/失效简历-导出", notes = "参数:isValid为1表示“有效”，为2表示“失效”")
	public String exportResume(@PathVariable Integer isValid) {
		return this.exportExcelService.exportResume(isValid);
	}
	
	@RequestMapping(value = "/exportPositive{flag}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "转正 所有待转正/待我处理-导出", notes = "参数:flag为1表示“待我处理”，为2表示“所有”")
	public String exportPositive(@PathVariable Integer flag,@RequestHeader String token) {
		return this.exportExcelService.exportPositive(flag,token);
	}
	
	@RequestMapping(value = "/exportEntry/{flag}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "入职 待我处理/所有待入职-导出", notes = "参数:flag为1表示“待我处理”，为2表示“所有”")
	public String exportEntry(@PathVariable Integer flag,@RequestHeader String token) {
		return this.exportExcelService.exportEntry(flag,token);
	}
	
	@RequestMapping(value = "/exportEmployeeInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "部门-员工信息导出", notes = "参数:null")
	public String exportEmployeeInfo(@RequestBody List<Integer> employeeIdList) {
		return this.exportExcelService.exportEmployeeInfo(employeeIdList);
	}
	@RequestMapping(value = "/downloadExportTemplate", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "部门-员工导入模板导出", notes = "参数:null")
	public String downloadExportTemplate() {
		return this.exportExcelService.downloadExportTemplate();
	}
	
	@RequestMapping(value = "/exportEmployeeInfoByDepId", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "部门下的员工信息导出", notes = "参数:部门ID")
	public String exportEmployeeInfoByDepId(@RequestBody List<Integer> depIdList) {
		return this.exportExcelService.exportEmployeeInfoByDepId(depIdList);
	}
	
	@RequestMapping(value = "/exportEmployeeInProjectInfo", method = RequestMethod.POST)
	@ApiOperation(value = "导出部门模块中员工在项信息", notes = "参数：")
	public RestResponse exportEmployeeInProjectInfo(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		return exportExcelService.exportEmployeeInProjectInfo(token,params);
	}
}
