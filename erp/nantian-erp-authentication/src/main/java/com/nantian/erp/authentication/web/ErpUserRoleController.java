package  com.nantian.erp.authentication.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.authentication.service.ErpUserRoleService;
import com.nantian.erp.common.rest.RestResponse;

@RestController
@Api(value = "用户角色管理接口")
@RequestMapping(value = "/userRole")
public class ErpUserRoleController {
	
	@Autowired
	private ErpUserRoleService erpUserRoleService;
	
	/**
	 * 提供给Hr工程调用添加用户角色 的接口
	 * @param userRole
	 * @return
	 */
	@ApiOperation(value = "添加用户角色信息", notes = "参数：[[ 用户角色对象 ]]", httpMethod="POST")
	@RequestMapping(value = "/insertUserRoleForHr", method = RequestMethod.POST)
	public RestResponse insertUserRoleForHr(@RequestParam Map<String,Object> userRole) {
		return this.erpUserRoleService.insertUserRoleForHr(userRole);
		}
	
	/**
	 * 提供给Hr工程调用添加用户角色 的接口
	 * @param userRoleList
	 * @return
	 */
	@ApiOperation(value = "添加用户角色信息", notes = "参数：[[ 用户角色对象 ]]", httpMethod="POST")
	@RequestMapping(value = "/insertUserRoleForHrList", method = RequestMethod.POST)
	public RestResponse insertUserRoleForHrList(@RequestParam Map<String,Object> userRole) {
		return this.erpUserRoleService.insertUserRoleForHrList(userRole);
		}
}
