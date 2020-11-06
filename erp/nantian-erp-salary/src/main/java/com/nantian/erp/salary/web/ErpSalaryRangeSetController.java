package com.nantian.erp.salary.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.model.ErpSalaryRangeSet;
import com.nantian.erp.salary.service.ErpSalaryRangeSetService;

/**
 * Description:薪资范围设置Controller
 * @author HouHuiRong
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年1月17日      		HouHuiRong          1.0       
 * </pre>
 * */
@RestController
@RequestMapping("salary/salaryRangeSet")
@Api(value="薪资范围设置")
public class ErpSalaryRangeSetController {
	
	@Autowired
	private ErpSalaryRangeSetService erpSalaryRangeSetService;
	
	@ApiOperation(value="根据职级查询薪资范围设置",notes="参数:[[]]")
	@RequestMapping(value="/findSalaryRangeSetByRank",method=RequestMethod.GET)
	public RestResponse findSalaryRangeSetByRank(@RequestParam Integer erpPositionNo){
		RestResponse result=this.erpSalaryRangeSetService.findSalaryRangeSetByRank(erpPositionNo);
		return result;
	}
	
//	@ApiOperation(value="查询所有职级薪资范围设置",notes="参数:[[]]")
//	@RequestMapping(value="/findAllSalaryRangeSet",method=RequestMethod.GET)
//	public RestResponse findAllSalaryRangeSet(){
//		RestResponse result=this.erpSalaryRangeSetService.findAllSalaryRangeSet();
//		return result;
//	}
	
	/**
	 * add by ZhangYuWei
	 */
	@ApiOperation(value="查询所有职级薪资范围设置",notes="参数:[[]]")
	@RequestMapping(value="/findAllSalaryRangeSet",method=RequestMethod.POST)
	public RestResponse findAllSalaryRangeSet(@RequestBody Map<String,Object> params,@RequestHeader String token){
		RestResponse result = this.erpSalaryRangeSetService.findAllSalaryRangeSet(params,token);
		return result;
	}
	
	/*
	 * 该接口暂时没有使用
	 */
	@ApiOperation(value = "新增薪资范围设置", notes = "参数：薪资范围设置表字段")
	@RequestMapping(value = "/insertSalaryRangeSet", method = RequestMethod.POST)
	public RestResponse insertSalaryRangeSet(@RequestBody ErpSalaryRangeSet erpSalaryRangeSet) {
		RestResponse result = erpSalaryRangeSetService.insertSalaryRangeSet(erpSalaryRangeSet);
		return result;
	}
	
	@ApiOperation(value = "修改薪资范围设置", notes = "参数：薪资范围设置表字段")
	@RequestMapping(value = "/updateSalaryRangeSet", method = RequestMethod.POST)
	public RestResponse updateSalaryRangeSet(@RequestBody ErpSalaryRangeSet erpSalaryRangeSet) {
		RestResponse result = erpSalaryRangeSetService.updateSalaryRangeSet(erpSalaryRangeSet);
		return result;
	}
	
	@ApiOperation(value = "删除职级薪资范围设置", notes = "参数：薪资范围设置表字段")
	@RequestMapping(value = "/deleteSalaryRangeSet", method = RequestMethod.GET)
	public RestResponse deleteSalaryRangeSet(@RequestParam Integer erpSalaryRangeSetId) {
		RestResponse result = erpSalaryRangeSetService.deleteSalaryRangeSet(erpSalaryRangeSetId);
		return result;
	}
}
