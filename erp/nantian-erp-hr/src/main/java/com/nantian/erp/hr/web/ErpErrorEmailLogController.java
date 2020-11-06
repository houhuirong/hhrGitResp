package com.nantian.erp.hr.web;

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
import com.nantian.erp.hr.data.model.ErrorEmailLog;
import com.nantian.erp.hr.service.ErpEmailLogService;
import com.nantian.erp.hr.service.ErpEmailServiceConfigService;

@RestController
@RequestMapping("erp/emailLog")
@Api(value = "邮件日志")
public class ErpErrorEmailLogController {
	@Autowired
	private ErpEmailLogService emailLogService;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	@RequestMapping(value = "/findEmailLogDetail", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有邮件日志", notes = "参数是[]")
	public RestResponse findEmailLogDetail(@RequestParam(required=false) Integer type,@RequestParam(required=false) String startTime,@RequestParam(required=false) String endTime) {
		RestResponse result = emailLogService.findEmailLogDetail(type,startTime,endTime);
		return result;
	}
	
	@RequestMapping(value = "/updateEmailLog", method = RequestMethod.POST)
	@ApiOperation(value = "修改邮件日志", notes = "参数是：[]")
	public RestResponse updateEmailLog(@RequestBody ErrorEmailLog errorEmailLog){
		RestResponse updateresult = null;
		try{
			updateresult = emailLogService.updateEmailLog(errorEmailLog);
		}catch (Exception e){
			logger.info("updateEmailLog方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
		return updateresult;
	}
	
	@RequestMapping(value = "/sendEmailLogById", method = RequestMethod.POST)
	@ApiOperation(value = "发送指定邮件", notes = "参数是：[]")
	public RestResponse sendEmailLogById(@RequestBody Map<String,Object> map) {
		RestResponse result = emailLogService.sendEmailLogById(map);
		return result;
	}
	
	@RequestMapping(value = "/downloadEmailAttachment", method = RequestMethod.GET)
	@ApiOperation(value = "部门调动邮件附件下载", notes = "部门调动邮件附件下载")
	public RestResponse downloadResume(@RequestParam String attachmentPath) {
		RestResponse result = emailLogService.downloadEmailAttachment(attachmentPath);
		return result;
	}
}
