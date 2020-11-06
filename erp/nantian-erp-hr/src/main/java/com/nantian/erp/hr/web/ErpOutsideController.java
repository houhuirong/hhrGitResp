package com.nantian.erp.hr.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpOutsideService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 外部服务controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年02月26日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("erp/outside")
@Api(value = "外部服务调用")
public class ErpOutsideController {
	
	@Autowired
	private ErpOutsideService outsideService;
	
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	@ApiOperation(value = "通过token获取当前登录员工信息", notes = "参数是：token")
	public RestResponse info(@RequestHeader String token) {
		RestResponse result = outsideService.info(token);
		return result;
	}
	
}
