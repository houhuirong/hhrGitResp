package com.nantian.erp.authentication.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.authentication.service.ErpUserService;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/erp")
@Api(value = "用户相关（供其他工程调用）")
public class ErpUserController {
	
	@Autowired
	private ErpUserService erpUserService;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "updateUserPermission", method = RequestMethod.POST)
	@ApiOperation(value = "通过username修改权限", notes = "通过username修改权限")
	public RestResponse updateUserPermission(@RequestBody Map<String,Object> param) {
		String username = param.get("username").toString();
		List<Integer> permissionList = (List<Integer>) param.get("permissionList");
		RestResponse result = erpUserService.updateUserPermission(username,permissionList);
		return result;
	}	

	@RequestMapping(value = "findRoleByUserName", method = RequestMethod.GET)
	@ApiOperation(value = "查询用户所有权限", notes = "查询用户所有权限")
	public RestResponse findRoleByUserName(@RequestParam String username) {
		List<ErpRole> roleList = erpUserService.findRoleByUserName(username);
		return RestUtils.returnSuccess(roleList);
	}
	
	@RequestMapping(value = "findRoleId", method = RequestMethod.GET)
	@ApiOperation(value = "查询用户登陆权限", notes = "查询用户登陆权限")
	public RestResponse findRoleId(@RequestParam String username) {
		String roleId = erpUserService.findRoleId(username);
		return RestUtils.returnSuccessWithString(roleId);
	}
	
	@RequestMapping(value="/insertErpUserForHr",method=RequestMethod.POST)
	@ApiOperation(value="新增用户信息", notes="参数[[]]", httpMethod="POST")
	public RestResponse insertErpUserForHr(@RequestParam Map<String,Object> map){
		Integer id=this.erpUserService.insertErpUserForHr(map);
		int i = Integer.parseInt(map.get("id").toString());
		 return RestUtils.returnSuccess(i);
	}
	
	@RequestMapping(value="/insertErpUserInfo",method=RequestMethod.POST)
	@ApiOperation(value="新增用户信息", notes="参数[[]]", httpMethod="POST")
	public RestResponse insertErpUserInfo(@RequestParam Map<String,String> map){
		Map<String, Object> param = new HashMap<String,Object>();
		param.put("userPhone",map.get("userPhone"));
		param.put("username", map.get("username"));
		param.put("userType", Integer.valueOf(map.get("userType")));
		param.put("userId", Integer.valueOf(map.get("userId")));
		param.put("password", map.get("password"));
		Integer n = this.erpUserService.insertErpUserInfo(param);
		return RestUtils.returnSuccess("插入用户信息成功:"+n);
	}
	
	@RequestMapping(value="/findAllErpUserInfo",method=RequestMethod.GET)
	@ApiOperation(value="查询所有用户信息", notes="参数[[]]", httpMethod="GET")
	public RestResponse findAllErpUserInfo(){
	  return this.erpUserService.findAllErpUserInfo();
	}
	@RequestMapping(value="/updateErpUserforForHr",method=RequestMethod.POST)
	@ApiOperation(value="提供给Hr调用修改用户信息", notes="参数[[]]", httpMethod="POST")
	public RestResponse updateErpUserforForHr(@RequestParam Map<String,Object> map){
		return this.erpUserService.updateErpUserforForHr(map);
	}
	@RequestMapping(value="/findErpUserByUserId",method=RequestMethod.POST)
	@ApiOperation(value="提供给Hr调用通过UserId查找用户信息", notes="参数[[]]", httpMethod="POST")
	public RestResponse findErpUserByUserId(@RequestParam Map<String,Object> map){
		return this.erpUserService.findErpUserByUserId(map);
	}
	
	@RequestMapping(value="/volidateErpUserPhone",method=RequestMethod.POST)
	@ApiOperation(value="提供给Hr调用验证手机号唯一", notes="参数[[]]", httpMethod="POST")
	public RestResponse volidateErpUserPhone(@RequestParam Map<String,Object> map){
		return this.erpUserService.volidateErpUserPhone(map);
	}
	@RequestMapping(value="/getErpUserForHr",method=RequestMethod.POST)
	@ApiOperation(value="提供给Hr调用通过Id查找用户信息", notes="参数[[]]", httpMethod="POST")
	public RestResponse getErpUserForHr(@RequestParam Map<String,Object> map){
		return this.erpUserService.getErpUserForHr(map);
	}
	
	@RequestMapping(value="/getErpUserForHrList",method=RequestMethod.POST)
	@ApiOperation(value="提供给Hr调用通过Id查找用户信息", notes="参数[[]]", httpMethod="POST")
	public RestResponse getErpUserForHrList(@RequestParam Map<String,Object> map){
		return this.erpUserService.getErpUserForHrList(map);
	}
	
	///nantian-erp/erp/findErpUserByUserIdArray
	@RequestMapping(value="/findErpUserByUserIdArray",method=RequestMethod.GET)
	@ApiOperation(value="提供给Hr调用通过多个UserId查找用户信息", notes="参数[[]]", httpMethod="POST")
	public RestResponse findErpUserByUserIdArray(@RequestParam String userId){
		return this.erpUserService.findErpUserByUserIdArray(userId);
	}
}
