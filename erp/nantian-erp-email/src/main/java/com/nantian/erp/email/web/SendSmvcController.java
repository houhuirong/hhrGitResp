package com.nantian.erp.email.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.email.service.SendSmvcService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 短信验证码(Short Message Verification Code)发送controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年02月21日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("secondarycheck/smvc")
@Api(value = "验证码发送")
public class SendSmvcController {
	@Autowired
	private SendSmvcService sendSmvcService;
	
	@RequestMapping(value = "/send", method = RequestMethod.POST)
	@ApiOperation(value = "发送验证码进行薪酬模块二级密码验证", notes = "参数是：发送验证码相关的必要参数")
	public RestResponse sendSmvc(@RequestBody Map<String,Object> smvcParams) {
		return sendSmvcService.sendSmvc(smvcParams);
	}
	
}
