package com.nantian.erp.hr.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpEmployeePostive;
import com.nantian.erp.hr.data.model.ErpPositiveRecord;
import com.nantian.erp.hr.data.vo.ErpDeployQueryVo;
import com.nantian.erp.hr.service.ErpPostiveService;

@RestController
@Api(value = "转正流程")
@RequestMapping("erp/postive")
public class ErpPostiveController {
	@Autowired
	private ErpPostiveService erpPostiveService;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@RequestMapping(value="findall", method = RequestMethod.GET)
	@ApiOperation(value = "所有待转正", notes = "所有待转正")
	public RestResponse waitingEntry(HttpServletRequest request){
		String token=request.getHeader("token");
		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
		List<Map<String,Object>> map = erpPostiveService.findAll(erpUser,token);
		return RestUtils.returnSuccess(map);
	}
	
	@RequestMapping(value = "waitingforme", method = RequestMethod.GET)
	@ApiOperation(value = "待我处理", notes = "待我处理")
	public RestResponse waitingForMe(HttpServletRequest request) {
		String token = request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser =(ErpUser) this.redisTemplate.opsForValue().get(token);
		List<Map<String,Object>> map = erpPostiveService.findByRole(erpUser,token);
		return RestUtils.returnSuccess(map);
	}
	
/*	@RequestMapping(value = "ontimehr", method = RequestMethod.POST)
	@ApiOperation(value = "按时转正hr", notes = "按时转正hr")
	public RestResponse ontimeHr(HttpServletRequest request, @RequestBody ErpOnTimeHrQueryVo onTimeHrQueryVo) {
		String token = request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		String username = stringRedisTemplate.opsForValue().get(token);
		if (!StringUtils.isNotBlank(username)) {
			return RestUtils.returnSuccessWithString("未登录");
		}
		String resultStr = erpPostiveService.ontimeHr(onTimeHrQueryVo, username);
		return RestUtils.returnSuccessWithString(resultStr);
	}*/
	
	
	@RequestMapping(value = "ontimedm", method = RequestMethod.GET)
	@ApiOperation(value = "按时转正部门经理", notes = "按时转正部门经理")
	public RestResponse ontimeDm(HttpServletRequest request, @RequestParam Integer employeeId,
			@RequestParam String content) {
		String token = request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		String username=erpUser.getUsername();
		if (!StringUtils.isNotBlank(username)) {
			return RestUtils.returnSuccessWithString("未登录");
		}
		String resultStr = erpPostiveService.ontimeDm(employeeId, content,erpUser);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value = "delay", method = RequestMethod.POST)
	@ApiOperation(value = "延期转正", notes = "延期转正")
	public RestResponse delay(HttpServletRequest request,@RequestBody ErpDeployQueryVo deployQueryVo) {
		String token = request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		ErpUser erpUser = (ErpUser)this.redisTemplate.opsForValue().get(token);
		String username=erpUser.getUsername();
		if (!StringUtils.isNotBlank(username)) {
			return RestUtils.returnSuccessWithString("未登录");
		}
		String resultStr = erpPostiveService.delay(deployQueryVo, erpUser, token);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value="findrecord", method = RequestMethod.GET)
	@ApiOperation(value = "查询待转正记录", notes = "查询待转正记录")
	public RestResponse findRecord(@RequestParam Integer employeeId){
		List<ErpPositiveRecord> list = erpPostiveService.findRecord(employeeId);
		return RestUtils.returnSuccess(list);
	}
	
	@RequestMapping(value="autoPosition",method=RequestMethod.POST)
	@ApiOperation(value = "超时未转正自动转正", notes = "超时未转正自动转正")
	public RestResponse automaticPosition(@RequestBody Map<String,Object> params){
		List<Integer> empIdList=erpPostiveService.automaticPosition(params);
		return RestUtils.returnSuccess(empIdList);
	}
	
	@RequestMapping(value="updateEmployeePostive",method=RequestMethod.POST)
	@ApiOperation(value = "更新实时薪资，更新员工转正状态", notes = "更新实时薪资，更新员工转正状态")
	public RestResponse updateEmployeePostive(@RequestBody ErpEmployeePostive params) {
		String response = this.erpPostiveService.updateEmployeePostive(params);
		if(response.contentEquals("OK")) {
			return RestUtils.returnSuccess("OK");
		}else {
			return RestUtils.returnSuccess("ERROR");
		}
	}

}
