package com.nantian.erp.hr.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 外部服务service
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
@Service
public class ErpOutsideService {

	@Autowired
	private RedisTemplate<Object,Object> redisTemplate;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Description: 根据当前登录人的token，返回该员工的信息
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月26日 下午16:04:07
	 */
	public RestResponse info(String token) {
		logger.info("进入info方法，参数是：token="+token);
		try {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从redis缓存中获取该用户的信息
			logger.info("redis中的用户信息："+erpUser);
			resultMap.put("userId", erpUser.getUserId());//当前登录人员工Id
			resultMap.put("employeeName", erpUser.getEmployeeName());//当前登录人名字
			resultMap.put("roles", erpUser.getRoles());//当前登录人角色列表
			return RestUtils.returnSuccess(resultMap);
//			String resultString = JSON.toJSONString(resultMap);//将结果转换为JSON串
//			return RestUtils.returnSuccessWithString(resultString);
		} catch (Exception e) {
			logger.error("info方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致查询失败！");
		}
	}
	
}
