package com.nantian.erp.authentication.service;

import com.nantian.erp.authentication.constants.DicConstants;
import com.nantian.erp.authentication.data.dao.ErpSysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 *
 * Description: 服务启动时，将员工信息同步到Redis中(Service)
 * @author xjh
 * @create 2020-05-21
 */
@Service
public class ErpAutoAfterStartService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ErpSysUserMapper erpSysUserMapper;

	/**
	 * Description：将员工邮箱，手机号写入redis，供查询
	 * @author xjh
	 * @create 2020-05-21
	 */
	public void saveUserInfoToRedis() {
		logger.info("saveUserInfoToRedis方法开始执行，传递参数：无");
		//查询全部的员工信息，将员工邮箱，手机号写入redis，供查询
		List<Map<String,Object>> userList = erpSysUserMapper.findAllUser();
		for (Map<String, Object> userInfo : userList) {
			Integer employeeId = null;
			if(userInfo.get("userId") != null){
				employeeId = Integer.valueOf(String.valueOf(userInfo.get("userId")));//员工Id
			}else{
				continue;
			}
			redisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_USER + employeeId, userInfo);
		}

	}
	
}
