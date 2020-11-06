package com.nantian.erp.hr.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpDimission;
import com.nantian.erp.hr.service.ErpDimissionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 员工离职controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月17日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("erp/dimission")
@Api(value = "离职")
public class ErpDimissionController {
	
	@Autowired
	private ErpDimissionService erpDimissonService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@RequestMapping(value="/findEmployeeDetail",method = RequestMethod.GET)
	@ApiOperation(value = "根据员工Id查询员工信息", notes = "参数是：员工编号")
	public RestResponse findEmployeeDetail(@RequestParam Integer employeeId){
		RestResponse employeeInfo = erpDimissonService.findEmployeeDetail(employeeId);
		return employeeInfo;
	}
	
	@RequestMapping(value = "/insertDimission", method = RequestMethod.POST)
	@ApiOperation(value = "保存离职申请", notes = "参数是：员工离职信息")
	public RestResponse insertDimission(@RequestHeader String token,@RequestBody ErpDimission dimission) {
		RestResponse insertResult = erpDimissonService.insertDimission(token,dimission);
		return insertResult;
	}
	
	@RequestMapping(value = "/updateDimission", method = RequestMethod.POST)
	@ApiOperation(value = "修改离职申请", notes = "参数是：员工离职信息")
	public RestResponse updateDimission(@RequestHeader String token,@RequestBody ErpDimission dimission){
		RestResponse updateresult = null;
		try{
			updateresult = erpDimissonService.updateDimission(token, dimission);
		}catch (Exception e){
			logger.info("findEmployeeAllDimission方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
		return updateresult;
	}
	
	@RequestMapping(value="/findEmployeeAllDimission",method = RequestMethod.GET)
	@ApiOperation(value = "查询所有的离职申请", notes = "无参数")
	public RestResponse findEmployeeAllDimission(@RequestHeader String token,@RequestParam Integer page, @RequestParam Integer rows,@RequestParam String key,@RequestParam String type){
		RestResponse allDimisionEmployee = erpDimissonService.findEmployeeAllDimission(token,page,rows,key,type);
		return allDimisionEmployee;
	}
	
	@RequestMapping(value = "/deleteDimission", method = RequestMethod.GET)
	@ApiOperation(value = "取消离职，通过离职表ID删除一条记录", notes = "参数是：离职表编号、员工编号")
	public RestResponse deleteDimission(@RequestHeader String token,@RequestParam Integer dimissionId,@RequestParam Integer employeeId) {
		RestResponse deleteResult = erpDimissonService.deleteDimission(token,dimissionId,employeeId);
		return deleteResult;
	}
	
	@RequestMapping(value = "/confirmDimission", method = RequestMethod.POST)
	@ApiOperation(value = "确认离职", notes = "参数是：办理手续时间")
	public RestResponse confirmDimission(@RequestHeader String token,@RequestBody ErpDimission dimission) {
		RestResponse updateResult = erpDimissonService.confirmDimission(token,dimission);
		return updateResult;
	}
	
	
	@RequestMapping(value = "/entryAgain", method = RequestMethod.POST)
	@ApiOperation(value = "离职员工再入职", notes = "参数是：员工ID、岗位ID、面试官员工ID、简历ID")
	public RestResponse entryAgain(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		return erpDimissonService.entryAgain(token,params);
	}
	
	
	
	/*@RequestMapping(value = "/dimission/{employeeId}/{id}/{time}", method = RequestMethod.GET)
	@ApiOperation(value = "离职", notes = "离职")
	public RestResponse dimission(@PathVariable Integer employeeId,@PathVariable Integer id,@PathVariable String time) {
		String resultStr = erpDimissonService.dimission(employeeId, id, time);
		return RestUtils.returnSuccessWithString(resultStr);
	}*/
	
	/*
	 * add by 曹秀斌
	 * 通过员工ID查询离职信息
	 */
	@ApiOperation(value = "根据员工ID查询信息", notes = "根据员工ID查询信息")
	@RequestMapping(value="/findOneById",method = RequestMethod.GET)
	public RestResponse findOneById(@RequestParam Integer employeeId){
		ErpDimission erpDimission = erpDimissonService.findOneById(employeeId);
		return RestUtils.returnSuccess(erpDimission);
	}
	
}
