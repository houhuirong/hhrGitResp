package com.nantian.erp.authentication.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nantian.erp.authentication.service.ErpSysButtonService;
import com.nantian.erp.common.rest.RestResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 权限管理-按钮资源管理接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   gaoxiaodong         1.0        
 * </pre>
 */

@RestController
@Api(value = "权限管理-按钮资源管理接口")
@RequestMapping(value = "/authentication/button")
public class ErpSysButtonController {
	
	@Autowired
	private ErpSysButtonService erpSysButtonService;

	@ApiOperation(value = "button设置—添加button列表", httpMethod = "POST")
	@RequestMapping(value = "/addButton", method = RequestMethod.POST)
	public RestResponse addButton(@RequestBody Map<String, Object> param) {
		return erpSysButtonService.addButton(param);
	}

	@ApiOperation(value = "button—修改button列表", httpMethod = "POST")
	@RequestMapping(value = "/updateButton", method = RequestMethod.POST)
	public RestResponse updateButton(@RequestBody Map<String, Object> param) {
		return erpSysButtonService.updateButton(param);
	}
	
	@ApiOperation(value = "button设置—删除button列表", httpMethod = "POST")
	@RequestMapping(value = "/delButton", method = RequestMethod.POST)
	public RestResponse delButton(@RequestBody Map<String, Object> param) {
		return erpSysButtonService.delButton(param);
	}
	
	@ApiOperation(value = "button设置—button编号去重", httpMethod = "GET")
	@RequestMapping(value = "/checkButtonNo", method = RequestMethod.GET)
	public RestResponse checkButtonNo(@RequestParam String buttonNo) {
		return erpSysButtonService.checkButtonNo(buttonNo);
	}
	
	@ApiOperation(value = "查询按钮权限信息（供其他工程调用）", notes = "菜单编号menuNo和角色列表roleIds")
	@RequestMapping(value = "/findButtonPrivilege", method = RequestMethod.POST)
	public RestResponse findButtonPrivilege(@RequestBody Map<String,Object> paramsMap) {
		RestResponse result = erpSysButtonService.findButtonPrivilege(paramsMap);
		return result;
	}
	
}
