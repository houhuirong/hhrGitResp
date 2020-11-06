package com.nantian.erp.email.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.email.service.SendEmailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 发送邮件controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月08日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("email/send")
@Api(value = "邮件发送")
public class SendEmailController {
	@Autowired
	private SendEmailService sendEmailService;
	
	@RequestMapping(value = "/withoutAttachment", method = RequestMethod.POST)
	@ApiOperation(value = "发送不带附件的邮件", notes = "参数是：发送邮件相关的必要参数")
	public RestResponse withoutAttachment(@RequestBody Map<String,String> emailParams) {
		return sendEmailService.withoutAttachment(emailParams);
	}
	
	@RequestMapping(value = "/withAttachments", method = RequestMethod.POST)
	@ApiOperation(value = "发送多附件的邮件", notes = "参数是：发送邮件相关的必要参数")
	public RestResponse withAttachments(@RequestBody Map<String,String> emailParams) {
		return sendEmailService.withAttachments(emailParams);
	}
	
	@RequestMapping(value = "/withAttachment", method = RequestMethod.POST)
	@ApiOperation(value = "发送单附件的邮件", notes = "参数是：发送邮件相关的必要参数")
	public RestResponse withAttachment(@RequestBody Map<String,String> emailParams) {
		return sendEmailService.withAttachment(emailParams);
	}
	
}
