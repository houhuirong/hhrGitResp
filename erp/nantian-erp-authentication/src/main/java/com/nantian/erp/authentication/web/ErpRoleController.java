package  com.nantian.erp.authentication.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.authentication.service.ErpRoleService;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 权限设置-角色管理接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0        
 * </pre>
 */
@RestController
@Api(value = "角色信息管理接口")
@RequestMapping(value = "/authentication/role")
public class ErpRoleController {
	
	@Autowired
	private ErpRoleService erpRoleService;
	
	@ApiOperation(value = "权限管理-根据当前用户动态加载菜单信息", notes = "参数：[[ token ]]", httpMethod="GET")
	@RequestMapping(value = "/load", method = RequestMethod.GET)
	public RestResponse load(HttpServletRequest request) {
		return this.erpRoleService.menuTreeLoad(request);
	}
	
	@ApiOperation(value = "角色管理-查询角色列表", notes = "参数 [[ ]]", httpMethod="GET")
	@RequestMapping(value = "/findAllRole", method = RequestMethod.GET)
	public RestResponse findAllRole(@RequestHeader String token, @RequestParam String roleType) {
		return this.erpRoleService.findAllRole(token, roleType);
	}
	
	@ApiOperation(value = "角色管理-新增角色信息", notes = "参数 [[ 角色ID ]]", httpMethod="POST")
	@RequestMapping(value = "/insertRole", method = RequestMethod.POST)
	public RestResponse insertRole(@RequestHeader String token, @RequestBody ErpRole erpRole) {
		return this.erpRoleService.insertRole(token, erpRole);
	}

	@ApiOperation(value = "角色管理-修改角色信息", notes = "参数 [[ 角色ID ]]", httpMethod="POST")
	@RequestMapping(value = "/updateRole", method = RequestMethod.POST)
	public RestResponse updateRole(@RequestHeader String token, @RequestBody ErpRole erpRole) {
		return this.erpRoleService.updateRole(token, erpRole);
	}
	
	@ApiOperation(value = "角色管理-删除角色信息", notes = "参数 [[ 角色ID ]]", httpMethod="GET")
	@RequestMapping(value = "/deleteRole", method = RequestMethod.GET)
	public RestResponse deleteRole(@RequestHeader String token, @RequestParam Integer roleId) {
		return this.erpRoleService.deleteRole(token, roleId);
	}
	
	@ApiOperation(value = "角色管理-获取所有的菜单信息树结构", notes = "参数：[[ roleId ]]", httpMethod="GET")
	@RequestMapping(value = "/getAllMenus", method = RequestMethod.GET)
	public RestResponse getAllMenus(@RequestParam Boolean findAll, @RequestParam Integer roleId) {
		return this.erpRoleService.getAllMenus(findAll, roleId);
	}
	
	@ApiOperation(value="角色管理-判断输入角色名或代号是否已存在", notes = "参数是：url对象", httpMethod="GET")
	@RequestMapping(value="/checkNameOrKeyword",method = RequestMethod.GET)
	public RestResponse checkNameOrKeyword(@RequestHeader String token, @RequestParam Boolean isChild, @RequestParam String paramNmae,@RequestParam int isName){
		return this.erpRoleService.checkNameOrKeyword(token, isChild, paramNmae,isName);
	}
	
	@ApiOperation(value="角色管理-查询所有基础角色列表",notes="参数[[ ]]",httpMethod="GET")
	@RequestMapping(value="/findAllBaseRole",method=RequestMethod.GET)
	public RestResponse findAllBaseRole(){
		return this.erpRoleService.findAllBaseRole();
	}
	
	@ApiOperation(value="角色管理-根据roleID查询角色信息",notes="参数[[ ]]",httpMethod="GET")
	@RequestMapping(value="/findRoleInfoByRoleId",method=RequestMethod.GET)
	public RestResponse findRoleInfoByRoleId(@RequestParam Integer roleId){
		return this.erpRoleService.findRoleInfoByRoleId(roleId);
	}	

	@ApiOperation(value = "菜单设置—查询菜单列表", httpMethod = "GET")
	@RequestMapping(value = "/findMenuByRole", method = RequestMethod.GET)
	public RestResponse findMenuByRole(@RequestParam Integer roleId) {
		return RestUtils.returnSuccess(this.erpRoleService.findMenuByRole(roleId));
	}

	@ApiOperation(value = "菜单设置—查询子角色", httpMethod = "GET")
	@RequestMapping(value = "/findChildRole", method = RequestMethod.GET)
	public RestResponse findChildRole(@RequestParam Integer roleId) {
		return this.erpRoleService.findChildRole(roleId);
	}
	
	@ApiOperation(value = "根据employeeId查询员工所具有的角色", httpMethod = "GET")
	@RequestMapping(value = "/findRoleListByEmpId", method = RequestMethod.GET)
	public RestResponse findRoleListByEmpId(@RequestParam Integer employeeId) {
		return this.erpRoleService.findRoleListByEmpId(employeeId);
	}
	
	@ApiOperation(value = "角色管理-修改角色授权", notes = "参数 [[ 角色ID ]]", httpMethod="POST")
	@RequestMapping(value = "/updateRoleAuth", method = RequestMethod.POST)
	public RestResponse updateRoleAuth(@RequestHeader String token, @RequestBody Map<String, Object> authMap) {
		return this.erpRoleService.updateRoleAuth(token, authMap);
	}	

	@ApiOperation(value="角色管理-查询授权的基础角色",notes="参数[[ ]]",httpMethod="GET")
	@RequestMapping(value="/findBaseRoleByAuth",method=RequestMethod.GET)
	public RestResponse findBaseRoleByAuth(@RequestHeader String token){
		return this.erpRoleService.findBaseRoleByAuth(token);
	}

	@ApiOperation(value="获取总裁、经管、HR及子角色ID、副总裁及子角色ID、一级部门经理角色及子角色ID、二级部门经理角色及子角色ID",notes="参数[[ ]]",httpMethod="GET")
	@RequestMapping(value="/findRoleIdsAndChildRoleIds",method=RequestMethod.GET)
	public RestResponse findRoleIdsAndChildRoleIds(@RequestHeader String token){
		return this.erpRoleService.findRoleIdsAndChildRoleIds(token);
	}
}
