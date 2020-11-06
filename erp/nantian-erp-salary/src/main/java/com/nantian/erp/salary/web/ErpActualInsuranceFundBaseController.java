package com.nantian.erp.salary.web;

import java.util.Map;

import com.nantian.erp.common.base.util.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.service.ErpActualInsuranceFundBaseService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 社保公积金基数controller
 *
 * @author 宋修功
 * @date 2020-02-18
 * @version 1.0
 */
@RestController
@RequestMapping("/salary/insuranceFund")
@Api(value = "工资单管理")
public class ErpActualInsuranceFundBaseController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ErpActualInsuranceFundBaseService erpActualInsuranceFundBaseService;
	
	//@ApiOperation(value = "插入或者更新员工的社保公积金基数")
	//@RequestMapping(value = "/insertInsurance", method = RequestMethod.POST)
	//@Scheduled(cron = "0/10 * * * * ?")
	@Scheduled(cron = "0 0 2 1 * ?")
	public RestResponse insertInsuranceFund() {
		RestResponse restResponse=null;
		try {
			return erpActualInsuranceFundBaseService.insertActualInsuranceFundBase();
		}catch (Exception e){
			logger.error("插入或者更新员工的社保公积金基数失败",e);
		}
		return restResponse;
	}
	
	@ApiOperation(value = "查询社保公积金基数")
	@RequestMapping(value = "/searchInsurance", method = RequestMethod.POST)
	public RestResponse searchInsurance(@RequestHeader String token, @RequestBody Map<String,Object> params) {
		RestResponse result = erpActualInsuranceFundBaseService.searchInsuranceFundByParameters(token, params);
		return result;
	}
}
