package com.nantian.erp.authentication.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nantian.erp.authentication.data.model.ErpSysMenu;
import com.nantian.erp.authentication.service.ErpSysMenuService;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Description: 菜单资源管理接口
 * 
 * @author caoxiubin
 * @version 1.0
 * 
 *          <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   gaoxiaodong         1.0
 *          </pre>
 */

@RestController
@Api(value = "菜单资源管理接口")
@RequestMapping(value = "/authentication/menu")
public class ErpSysMenuController {
	
	@Autowired
	private ErpSysMenuService erpSysMenuService;

	@ApiOperation(value = "菜单设置—查询菜单列表", httpMethod = "GET")
	@RequestMapping(value = "/findAllMenu", method = RequestMethod.GET)
	public RestResponse findAllMenu() {
		return RestUtils.returnSuccess(this.erpSysMenuService.findAllMenu());
	}

	@ApiOperation(value = "菜单设置—删除菜单列表", httpMethod = "POST")
	@RequestMapping(value = "/delMenu", method = RequestMethod.POST)
	public RestResponse delMenu(@RequestBody ErpSysMenu erpSysMenu) {
		String str = erpSysMenuService.delMenu(erpSysMenu);
		return RestUtils.returnSuccess(str);
	}

	@ApiOperation(value = "菜单设置—添加菜单列表", httpMethod = "POST")
	@RequestMapping(value = "/addMenu", method = RequestMethod.POST)
	public RestResponse addMenu(@RequestBody ErpSysMenu erpSysMenu) {
		return erpSysMenuService.addMenu(erpSysMenu);
	}

	@ApiOperation(value = "菜单设置—修改菜单列表", httpMethod = "POST")
	@RequestMapping(value = "/updateMenu", method = RequestMethod.POST)
	public RestResponse updateMenu(@RequestBody Map<String, Object> param) {
		return erpSysMenuService.updateMenu(param);
	}
	
	@ApiOperation(value = "菜单设置—菜单编号去重", httpMethod = "GET")
	@RequestMapping(value = "/checkMenuNo", method = RequestMethod.GET)
	public RestResponse checkMenuNo(@RequestParam String menuNo) {
		return erpSysMenuService.checkMenuNo(menuNo);
	}	
}
