package com.nantian.erp.salary.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.model.ErpTalkSalary;
import com.nantian.erp.salary.service.ErpTalkSalaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 面试谈薪controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月14日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("salary/talkSalary")
@Api(value = "面试通过-供HR工程调用存入面试谈薪结果")
public class ErpTalkSalaryController {
	
	@Autowired
	private ErpTalkSalaryService erpTalkSalaryService;
	
	@ApiOperation(value = "新增面试谈薪信息", notes = "参数是：薪酬数据")
	@RequestMapping(value = "/insertErpTalkSalary", method = RequestMethod.POST)
	public RestResponse insertErpTalkSalary(@RequestBody ErpTalkSalary talkSalary) {
		return this.erpTalkSalaryService.insertErpTalkSalary(talkSalary);
	}
	
	@ApiOperation(value = "查询面试谈薪信息", notes = "参数是：offerId")
	@RequestMapping(value = "/findErpTalkSalary", method = RequestMethod.GET)
	public RestResponse findErpTalkSalary(@RequestParam Integer offerId) {
		return this.erpTalkSalaryService.findErpTalkSalary(offerId);
	}
	
	@ApiOperation(value = "修改面试谈薪信息", notes = "参数是：薪酬数据")
	@RequestMapping(value = "/updateErpTalkSalary", method = RequestMethod.POST)
	public RestResponse updateErpTalkSalary(@RequestBody ErpTalkSalary talkSalary,@RequestHeader String token) {
		return this.erpTalkSalaryService.updateErpTalkSalary(talkSalary, token);
	}	
}
