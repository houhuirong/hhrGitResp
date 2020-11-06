package com.nantian.erp.authentication.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.nantian.erp.authentication.data.dao.ErpRoleMapper;
import com.nantian.erp.authentication.data.dao.ErpUserRoleMapper;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 用户角色管理接口
 * @author lx
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 *                  lx         1.0        
 * </pre>
 */
@Service
public class ErpUserRoleService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	RestTemplate restTemplate;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate; 
	
	@Autowired
	private ErpRoleMapper erpRoleMapper;
	
	@Autowired
	private  ErpUserRoleMapper userRoleMapper;
	
	
	/**
	 * 添加用户角色
	 * @param userRole
	 * @return
	 */
	@Transactional
	public RestResponse insertUserRoleForHr(Map<String, Object> userRole){
		logger.info("insertUserRoleForHr开始执行");
		try {
			userRoleMapper.insertUserRoleForHr(userRole);
		} catch (Exception e) {
			logger.error("插入用户角色表异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("插入用户角色表异常:"+e.getMessage());
		}
		
		return RestUtils.returnSuccessWithString("success");
	}


	@SuppressWarnings("unchecked")
	@Transactional
	public RestResponse insertUserRoleForHrList(Map<String,Object> userRole) {
		logger.info("insertUserRoleForHrList开始执行,参数userRole:"+userRole.toString());
		try {
			if(!userRole.isEmpty()){
			String str=String.valueOf(userRole.get("userRoleList"));
			List<Object> list=JSON.parseArray(str);
			List< Map<String,Object>> listw = new ArrayList<Map<String,Object>>();
			for(Object object:list){
				Map<String,Object> ret=(Map<String, Object>) object;
				listw.add(ret);
			}
			userRoleMapper.insertUserRoleForHrList(listw);
			}
		} catch (Exception e) {
			logger.error("insertUserRoleForHrList插入用户角色表方法异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("插入用户角色表异常:"+e.getMessage());
		}
		
		return RestUtils.returnSuccessWithString("success");
	}
	
	
}

