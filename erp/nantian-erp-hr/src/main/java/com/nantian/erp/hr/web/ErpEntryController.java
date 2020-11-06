package com.nantian.erp.hr.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpEntryRecord;
import com.nantian.erp.hr.data.vo.ErpEmployeeQueryVo;
import com.nantian.erp.hr.service.ErpEntryServcie;

@RestController
@RequestMapping("erp/entry")
@Api(value = "入职流程")
public class ErpEntryController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate; 
	
	@Autowired
	private ErpEntryServcie erpEntryServcie;
	
	@SuppressWarnings({ "rawtypes" })
	@Autowired
	private RedisTemplate redisTemplate; 
	
	@RequestMapping(value="findall", method = RequestMethod.GET)
	@ApiOperation(value = "所有待入职", notes = "所有待入职")
	public RestResponse findall(@RequestParam String token){
		Map<String, Object> map = erpEntryServcie.findAll(token);
		return RestUtils.returnSuccess(map);
	}
	
	@RequestMapping(value="findAllByYear", method = RequestMethod.GET)
	@ApiOperation(value = "年度入职", notes = "年度入职")
	public RestResponse findAllByYear(@RequestParam String token,@RequestParam String startTime,
			@RequestParam String endTime){
		Map<String, Object> map = erpEntryServcie.findAll(token,startTime,endTime);
		return RestUtils.returnSuccess(map);
	}
	
	@RequestMapping(value = "waitingforme", method = RequestMethod.GET)
	@ApiOperation(value = "待我处理", notes = "待我处理")
	public RestResponse waitingForMe(HttpServletRequest request,HttpServletResponse response) {
		
		//List<Map<String, Object>> mapList = null;		
		Map<String, Object> mapList = null;		
		try {
			String token = request.getHeader("token");
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);		//获取登陆用户信息
			mapList = erpEntryServcie.findByRole(erpUser);
		} catch (Exception e) {
			logger.error("waitingForMe发生异常："+e.getMessage(),e);
		}
		return RestUtils.returnSuccess(mapList);
	}
	
	@RequestMapping(value = "updateemployee", method = RequestMethod.POST)
	@ApiOperation(value = "填写入职相关信息", notes = "填写入职相关信息")
	public RestResponse updateEmployee(HttpServletRequest request, @RequestBody ErpEmployeeQueryVo employeeQueryVo) {
		String token = request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		String username=erpUser.getUsername();
		if (!StringUtils.isNotBlank(username)) {
			return RestUtils.returnSuccessWithString("未登录");
		}

		try {
			return erpEntryServcie.updateEmployee(employeeQueryVo, erpUser,token); 
		} catch (DuplicateKeyException e) {
			return RestUtils.returnSuccessWithString("用户名重复");
		} catch (Exception e) {
			logger.error("updateEmployee出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("failure");
		}
	}
	
	@RequestMapping(value="/addEntry",method = RequestMethod.POST)
	@ApiOperation(value = "新增入职", notes = "参数是：token、复试信息PO")
	public RestResponse waitingForMeSocialReexamJudge(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpEntryServcie.addEntry(token,params);
		return result;
	}
	
	@RequestMapping(value = "updateprojectinfo", method = RequestMethod.GET)
	@ApiOperation(value = "填写项目信息", notes = "填写项目信息")
	public RestResponse updateProjectInfo(HttpServletRequest request, @RequestParam Integer projectId,
			@RequestParam Integer offerId) {
		String token = request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		String username=erpUser.getUsername();
		if (!StringUtils.isNotBlank(username)) {
			return RestUtils.returnSuccessWithString("未登录");
		}
		String resultStr = erpEntryServcie.updateProjectInfo(projectId, erpUser, offerId);
		return RestUtils.returnSuccessWithString(resultStr);
	}

	@RequestMapping(value = "cancelentry", method = RequestMethod.GET)
	@ApiOperation(value = "取消入职", notes = "取消入职")
	public RestResponse cancelEntry(HttpServletRequest request, 
			@RequestParam Integer resumeId,@RequestParam Integer offerId,@RequestParam String info) {
		String token = request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}

		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		String username=erpUser.getUsername();
		if (!StringUtils.isNotBlank(username)) {
			return RestUtils.returnSuccessWithString("未登录");
		}
		String resultStr = erpEntryServcie.cancelEntry(resumeId,offerId,info,erpUser.getEmployeeName(), erpUser.getUserId());
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value="findrecord", method = RequestMethod.GET)
	@ApiOperation(value = "查询待入职记录", notes = "查询待入职记录")
	public RestResponse findRecord(@RequestParam Integer offerId){
		List<ErpEntryRecord> list = erpEntryServcie.findRecord(offerId);
		return RestUtils.returnSuccess(list);
	}
	
	@RequestMapping(value="findAllBaseRole",method=RequestMethod.GET)
	@ApiOperation(value="查询所有基础角色",notes="查询所有基础角色")
	public RestResponse findAllBaseRole(HttpServletRequest request){
		String token=request.getHeader("token");
		List<Object> list=erpEntryServcie.findAllBaseRole(token);
		return RestUtils.returnSuccess(list);
	}
	
	
	/**
	 * Description: 通过当前登陆用户的角色信息来判断是否可以访问该接口-访问接口验证
	 * @param  request       
	 * @param  response       
	 * @return boolean             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月8日 上午11:32:53
	 */
	private boolean isTrue(String token,HttpServletRequest request,HttpServletResponse response) {
		
		boolean flag = false;		//flag标记是标志该角色是否有访问接口的权限： true为有，false为无	
		ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);	//获取登陆用户信息
		List<Integer> roles = erpUser.getRoles();		//一个用户可能存在多个角色		
		String requestUrl = request.getRequestURI();		//获取当前接口请求路径
		for (Integer roleId : roles) {
			//获取redis缓存的接口路径信息  key由角色ID+请求路径组成
			String redisPathUrl = this.stringRedisTemplate.opsForValue().get(roleId+"_"+requestUrl);
			if(null != redisPathUrl && !"".equals(redisPathUrl) && requestUrl.equals(redisPathUrl)) {
				flag = true;
				break;
			}
		}
		return flag;
	}
}
