package  com.nantian.erp.authentication.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.authentication.data.vo.ErpSysUserVo;
import com.nantian.erp.authentication.service.ErpSysUserService;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 权限设置-用户管理接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月14日                   caoxiubin         1.0        
 * </pre>
 */
@RestController
@Api(value = "用户信息管理接口")
@RequestMapping(value = "/authentication/user")
public class ErpSysUserController {
	
	@Autowired
	private ErpSysUserService erpSysUserService;
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ApiOperation(value = "用户登录", notes = "用户登录")
	public RestResponse login(@RequestBody ErpUser user) {
		RestResponse login = this.erpSysUserService.login(user);
		return login;
	}
	
	@ApiOperation(value = "修改用户密码", notes = "参数是：用户ID、旧密码、新密码")
	@RequestMapping(value = "/updateUserPassword", method = RequestMethod.POST)
	public RestResponse updateUserPassword(@RequestBody ErpUser erpUser){
		RestResponse result = erpSysUserService.updateUserPassword(erpUser);
		return result;
	}
	
	@RequestMapping(value = "/checkFirstLogin", method = RequestMethod.GET)
	@ApiOperation(value = "检验用户有没有二级密码", notes = "参数是：用户Id")
	public RestResponse checkFirstLogin(@RequestParam Integer id) {
		RestResponse result = erpSysUserService.checkFirstLogin(id);
		return result;
	}
	
	@RequestMapping(value = "/registerSecondaryPassword", method = RequestMethod.POST)
	@ApiOperation(value = "用户注册自己的二级密码", notes = "参数是：用户Id、二级密码")
	public RestResponse registerSecondaryPassword(@RequestBody ErpUser user) {
		RestResponse result = erpSysUserService.registerSecondaryPassword(user);
		return result;
	}
	
	@RequestMapping(value = "/loginSecondaryPassword", method = RequestMethod.POST)
	@ApiOperation(value = "用户登录二级密码", notes = "参数是：用户Id、二级密码")
	public RestResponse loginSecondaryPassword(@RequestBody ErpUser user) {
		RestResponse result = erpSysUserService.loginSecondaryPassword(user);
		return result;
	}
	
	@ApiOperation(value = "用户修改二级密码", notes = "参数是：用户Id、旧二级密码、新二级密码")
	@RequestMapping(value = "/updateUserSecondaryPassword", method = RequestMethod.POST)
	public RestResponse updateUserSecondaryPassword(@RequestBody ErpUser erpUser){
		RestResponse result = erpSysUserService.updateUserSecondaryPassword(erpUser);
		return result;
	}
	
	@ApiOperation(value = "用户管理-查询用户列表", notes = "参数 [[ 无参数 ]]", httpMethod="GET")
	@RequestMapping(value = "/findAllUser", method = RequestMethod.GET)
	public RestResponse findAllUser(@RequestHeader String token, @RequestParam String getAllFlag, @RequestParam(required = false) Boolean isDimissionPage) {
		return this.erpSysUserService.findAllUser(token, getAllFlag, isDimissionPage);
	}
	
	@ApiOperation(value = "用户管理-查询临时用户列表", notes = "参数 [[ 无参数 ]]", httpMethod="GET")
	@RequestMapping(value = "/findAllTempUser", method = RequestMethod.GET)
	public RestResponse findAllUser(@RequestHeader String token) {
		return this.erpSysUserService.findAllTempUser(token);
	}

	@ApiOperation(value = "用户管理-修改用户名", notes = "参数 [[ 用户信息 ]]", httpMethod="POST")
	@RequestMapping(value = "/changeUserName", method = RequestMethod.POST)
	public RestResponse changeUserName(@RequestHeader String token, @RequestBody Map<String,Object> map) {
		return this.erpSysUserService.changeUserName(token, map);
	}
	
	@ApiOperation(value = "用户管理-新增用户信息", notes = "参数 [[ 用户信息 ]]", httpMethod="POST")
	@RequestMapping(value = "/insertUser", method = RequestMethod.POST)
	public RestResponse insertUser(@RequestHeader String token, @RequestBody ErpSysUserVo erpSysUserVo) {
		return this.erpSysUserService.insertUser(token,erpSysUserVo);
	}

	@ApiOperation(value = "用户管理-修改用户信息", notes = "参数 [[ 用户信息 ]]", httpMethod="POST")
	@RequestMapping(value = "/updateUser", method = RequestMethod.POST)
	public RestResponse updateUser(@RequestHeader String token, @RequestBody ErpSysUserVo erpSysUserVo) {
		return this.erpSysUserService.updateUser(token, erpSysUserVo);
	}
	
	@ApiOperation(value = "用户管理-删除用户信息", notes = "参数 [[ userId ]]", httpMethod="GET")
	@RequestMapping(value = "/deleteUser", method = RequestMethod.GET)
	public RestResponse deleteUser(@RequestParam Integer userId) {
		return this.erpSysUserService.deleteUser(userId);
	}
	
	@ApiOperation(value = "用户管理-用户名去重", notes = "参数 [[ username ]]", httpMethod="GET")
	@RequestMapping(value = "/checkUsername", method = RequestMethod.GET)
	public RestResponse checkUsername(@RequestParam String username) {
		return this.erpSysUserService.checkUsername(username);
	}
	
	@ApiOperation(value = "用户管理-查询所有员工信息", notes = "参数 [[  ]]", httpMethod="GET")
	@RequestMapping(value = "/findAllEmp", method = RequestMethod.GET)
	public RestResponse findAllEmp(HttpServletRequest request) {
		return this.erpSysUserService.findAllEmp(request);
	}
	
	@ApiOperation(value="用户管理-根据员工ID查询员工信息",notes="参数[[]]",httpMethod="GET")
	@RequestMapping(value="/findUserByEmpId",method=RequestMethod.GET)
	public RestResponse findUserByEmpId(@RequestParam Integer empId){
		return this.erpSysUserService.findUserByEmpId(empId);
	}
	
	//add by ZhangYuWei 定时任务接口，无须传递token
	@ApiOperation(value="用户管理-根据员工ID查询员工信息",notes="员工ID")
	@GetMapping(value="/findUserByEmpIdScheduler")
	public RestResponse findUserByEmpIdScheduler(@RequestParam Integer empId){
		return this.erpSysUserService.findUserByEmpId(empId);
	}
	
	@ApiOperation(value="用户管理-根据用户ID查询员工信息",notes="参数[[]]",httpMethod="GET")
	@RequestMapping(value="/findUserInfoByUserId",method=RequestMethod.GET)
	public RestResponse findUserInfoByUserId(@RequestParam Integer userId){
		return this.erpSysUserService.findUserInfoByUserId(userId);
	}
	// add by lx
	@ApiOperation(value = "修改用户信息forHr", notes = "修改用户信息forHr", httpMethod="POST")
	@RequestMapping(value = "/updateUserForHr", method = RequestMethod.POST)
	public RestResponse updateUserForHr(@RequestParam Map<String,Object> map) {
		return this.erpSysUserService.updateUserForHr(map);
	}
	
	@ApiOperation(value="用户管理-根据员工ID查询员工信息",notes="参数[[]]",httpMethod="POST")
	@RequestMapping(value="/findUserByEmpIds",method=RequestMethod.POST)
	public RestResponse findUserByEmpIds(@RequestParam Map<String,Object> map){
		return this.erpSysUserService.findUserByEmpIds(map);
	}
	
	@ApiOperation(value="提供给Hr调用通过Id查找用户列表", notes="参数[[]]", httpMethod="POST")
	@RequestMapping(value="/findEmpIdListByUserId",method=RequestMethod.POST)	
	public RestResponse findEmpIdListByUserId(@RequestParam Map<String,Object> map){
		return this.erpSysUserService.findEmpIdListByUserId(map);
	}

	@RequestMapping(value = "findBaseRoleByUser", method = RequestMethod.GET)
	@ApiOperation(value = "查询用户所有权限", notes = "查询用户所有权限")
	public RestResponse findBaseRoleByUser(@RequestHeader String token) {
		List<ErpRole> roleList = erpSysUserService.findBaseRoleByUser(token);
		return RestUtils.returnSuccess(roleList);
	}	

	/**
	 * 权限管理模块
	 * 短信验证
	 * Copyright: Copyright (C) 2019 Nantian, Inc. All rights reserved.
	 * Company: 北京南天软件有限公司
	 *
	 * @author libaokun
	 * @since 2019-02-19 23:24
	 */
	@RequestMapping(value = "salary/secondarycheck/sendsms", method = RequestMethod.GET)
	@ApiOperation(value = "发送验证短信", notes = "参数：用户id")
	public RestResponse sendSms(@RequestParam String token) {
		return this.erpSysUserService.sendSms(token);
	}

	@RequestMapping(value = "salary/secondarycheck/checksms", method = RequestMethod.GET)
	@ApiOperation(value = "校验验证短信", notes = "参数：验证码")
	public RestResponse checkSms(@RequestParam String code,@RequestParam String token) {
		return this.erpSysUserService.checkSms(code, token);
	}
	
	/*
	 * 找回密码（发送验证码、通过验证码校验）
	 */
	@ApiOperation(value = "找回密码-发送验证码", notes = "参数是：用户登录名（邮箱）")
	@RequestMapping(value = "/findPasswordSend", method = RequestMethod.GET)
	public RestResponse findPasswordSend(@RequestParam String username){
		RestResponse result = erpSysUserService.findPasswordSend(username);
		return result;
	}
	
	@ApiOperation(value = "找回密码-通过验证码重置密码", notes = "参数是：用户登录名（邮箱）、收到的验证码")
	@RequestMapping(value = "/findPasswordReset", method = RequestMethod.POST)
	public RestResponse findPasswordReset(@RequestBody Map<String,Object> params){
		RestResponse result = erpSysUserService.findPasswordReset(params);
		return result;
	}
	
	@ApiOperation(value = "通过角色ID查询一个角色对应的所有用户信息", notes = "参数是：角色ID")
	@RequestMapping(value = "/findAllUserByRoleId", method = RequestMethod.GET)
	public RestResponse findAllUserByRoleId(@RequestParam Integer roleId){
		return  erpSysUserService.findAllUserByRoleId(roleId);
	}
	
	@ApiOperation(value="通过手机号查询用户信息",notes="参数是:userPhone")
	@RequestMapping(value="/findUserByMobile",method=RequestMethod.POST)
	public RestResponse findUserByMobile(@RequestBody Map<String, Object> param){
		return this.erpSysUserService.findUserByMobile(param);
	}
	
	@ApiOperation(value="通过手机号列表批量查询用户信息",notes="参数是:userPhone")
	@RequestMapping(value="/findMobileByUserList",method=RequestMethod.POST)
	public RestResponse findMobileByUserList(@RequestBody Map<String, Object> param){
		return this.erpSysUserService.findMobileByUserList(param);
	}
}
