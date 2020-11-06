package com.nantian.erp.hr.web;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
import com.nantian.erp.hr.data.model.TEmailServiceConfig;
import com.nantian.erp.hr.service.ErpEmailServiceConfigService;

@RestController
@RequestMapping("erp/emailConfig")
@Api(value = "邮件日志")
public class ErpEmailServiceConfigController {
	@Autowired
	private ErpEmailServiceConfigService emailServiceConfigService;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@RequestMapping(value = "/findEmailConfigDetail", method = RequestMethod.GET)
	@ApiOperation(value = "查询邮件服务配置详情", notes = "参数是：[]")
	public RestResponse findEmailConfigDetail(@RequestParam(required=false) Integer send) {
		return emailServiceConfigService.findEmailConfigDetail(send);
	}
	@RequestMapping(value = "/updateEmailConfig", method = RequestMethod.POST)
	@ApiOperation(value = "修改邮件服务配置", notes = "参数是：[]")
	public RestResponse updateEmailConfig(@RequestBody TEmailServiceConfig emailServiceConfig){
		RestResponse updateresult = null;
		try{
			updateresult = emailServiceConfigService.updateEmailConfig(emailServiceConfig);
		}catch (Exception e){
			logger.info("updateEmailConfig方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
		return updateresult;
	}
}
