package com.nantian.erp.authentication.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.models.auth.In;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.nantian.erp.authentication.constants.DicConstants;
import com.nantian.erp.authentication.data.dao.ErpRoleMapper;
import com.nantian.erp.authentication.data.dao.ErpUserMapper;
import com.nantian.erp.authentication.data.dao.ErpUserRoleMapper;
import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.authentication.data.model.ErpUserRole;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;


@Service
public class ErpUserService {

	private final Logger logger=LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ErpUserMapper mapper;
	@Autowired
	private ErpRoleMapper erpRoleMapper;	
	@Autowired
	private ErpUserRoleMapper erpUserRoleMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	
	/**
	 * 修改用户权限
	 * @param 用户ID、权限列表
	 */
	@Transactional(readOnly = false)
	public RestResponse updateUserPermission(String username, List<Integer> permissionList){
		Integer userId = mapper.findIdByName(username);
		//删除userId所有关联的用户角色
		erpUserRoleMapper.deleteUserRoleByUserId(userId);
		
		ErpUserRole userRole = new ErpUserRole();
		userRole.setuId(userId);
		for(Integer rId : permissionList){
			userRole.setrId(rId);
			erpUserRoleMapper.insertUserRole(userRole);
		}
		
		return RestUtils.returnSuccessWithString("OK");
	}
	
	/**
	 * add by 张倩 20180926
	 * 根据员工名查询现有权限
	 * @return
	 */
	public List<ErpRole> findRoleByUserName(String username) {
		List<ErpRole> roleList = mapper.findRoleByUserName(username);
		return roleList;
	}
	
	/**
	 * add by 曹秀斌 2018-10-09
	 * 根据员工名查询登陆权限
	 * @return
	 */
	public String findRoleId(String username) {
		return mapper.findRoleId(username);
	}
	
	/**
	 * Description: 用户信息-新增用户信息
	 * @param  map  用户对象 带有用户名、密码和电话号码等     
	 * @return RestResponse             
	 * @Author houhuirong
	 * @Create Date: 2018年10月24日 上午16:21:53
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public Integer insertErpUserForHr(Map<String,Object> map) {
		logger.info("insertErpUserForHr开始执行,参数map:"+map.toString());
		Integer id=null; //最新插入数据后自增的主键						
		
		try{
			//通过员工id查找用户是否存在
			Map<String, Object> erpUser = this.mapper.findErpUserByUserId(map);			
			if (erpUser != null){
				//更新
				id = (Integer) erpUser.get("id");
				map.put("id", id);
				
				this.mapper.updateErpUserforForHr(map);
				
			}else{
				//插入
				id=this.mapper.insertErpUserForHr(map);
			}
			Map<String,Object> redisMap=new HashMap<String, Object>();
			redisMap.put("id", id);
			redisMap.put("username", map.get("username"));
			redisMap.put("userId", map.get("userId"));
			redisMap.put("userPhone", map.get("userPhone"));
			redisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_USER + Integer.valueOf(String.valueOf(map.get("userId"))), redisMap);
		}catch(Exception e){
			logger.error("用户信息-新增用户insertErpUserForHr方法出现错误："+e.getMessage(),e);
		}
		return id;
	}

	@Transactional
	public Integer insertErpUserInfo(Map<String, Object> param) {
		logger.info("insertErpUserInfo方法开始执行,参数param:"+param.toString());
		Integer id=null; //最新插入数据后自增的主键
		try{
			id=this.mapper.insertErpUserForHr(param);
		}catch(Exception e){
			logger.error("用户信息-新增用户insertErpUserInfo方法出现错误："+e.getMessage(),e);
		}
		return  id;
	}
	
	/**
	 * 查询所有用户信息
	 * @return
	 */
	public RestResponse findAllErpUserInfo() {
		logger.info("findAllErpUserInfo方法开始执行，无参数");
		List<Map<String, Object>>  list = null;
		try {
			  list = this.mapper.findAllErpUser();

			for (Map<String,Object> user : list) {
				List<Integer> roleIdList = erpRoleMapper.findRoleIdListByEmpId(Integer.valueOf(String.valueOf(user.get("userId"))));
				user.put("roleIdList", roleIdList);
			}

		} catch (Exception e) {
			logger.error("调用findAllErpUserInfo()方法出错"+e.getMessage(),e);
			return RestUtils.returnFailure("调用findAllErpUserInfo()方法出错"+e.getMessage());
		}
		
		return RestUtils.returnSuccess(list, "查询成功");
	}
	
	/**
	 * 提供给Hr调用修改用户信息
	 * @param map
	 * @return
	 */
	@Transactional
	public RestResponse updateErpUserforForHr(Map<String, Object> map) {
		logger.info("updateErpUserforForHr方法开始执行,参数map:"+map.toString());
		try{
			this.mapper.updateErpUserforForHr(map);
		}catch(Exception e) {
			logger.error("调用updateErpUserforForHr()方法出错"+e.getMessage(),e);
			return RestUtils.returnFailure("调用updateErpUserforForHr()方法出错"+e.getMessage());
		}
		
		return  RestUtils.returnSuccess("修改成功");
	}


	public RestResponse findErpUserByUserId(Map<String, Object> map) {
		logger.info("findErpUserByUserId方法开始执行,参数map:"+map.toString());
		Map<String, Object> erpUser = new HashMap<>();
		List<Map<String,Object>> roleList = new ArrayList<Map<String,Object>>();
		List<Integer> roleIdList = new ArrayList<Integer>();

		try{
			erpUser =  this.mapper.findErpUserByUserId(map);
			roleIdList = erpRoleMapper.findRoleIdListByEmpId(Integer.valueOf(String.valueOf(map.get("userId"))));
			if(erpUser!=null){
				erpUser.put("roleIdList", roleIdList);
			}

		}catch(Exception e) {
			logger.error("调用findErpUserByUserId()方法出错"+e.getMessage(),e);
			return RestUtils.returnFailure("调用findErpUserByUserId()方法出错"+e.getMessage());
		}
		
		return  RestUtils.returnSuccess(erpUser);
	}

	/**
	 * 验证手机号 唯一性
	 * @param map
	 * @return
	 */
	public RestResponse volidateErpUserPhone(Map<String, Object> map) {
		Integer n = null;
		try{
			n =  this.mapper.volidateErpUserPhone(map);
		}catch(Exception e) {
			
			logger.error("调用volidateErpUserPhone()方法出错"+e.getMessage(),e);
			return RestUtils.returnFailure("调用volidateErpUserPhone()方法出错"+e.getMessage());
		}
		return RestUtils.returnSuccess(n);
	}
	
	/**
	 * 提供给Hr调用通过Id查找用户信息
	 * @param map
	 * @return
	 */
	public RestResponse getErpUserForHr(Map<String, Object> map) {
		logger.info("getErpUserForHr方法开始执行,参数map:"+map.toString());
		ErpUser user = null;
		try{
			user =  this.mapper.getErpUserForHr(map);
		}catch(Exception e) {
			
			logger.info("调用getErpUserForHr()方法出错"+e.getMessage(),e);
			return RestUtils.returnFailure("调用getErpUserForHr()方法出错"+e.getMessage());
		}
		return RestUtils.returnSuccess(user);
	}


	@SuppressWarnings("unchecked")
	public RestResponse getErpUserForHrList(Map<String,Object> map) {
		logger.info("getErpUserForHrList方法开始执行，参数map:"+map.toString());
		List<Map<String,Object>> resultList=null;
		Integer[] curPersonIds=null;
		int count=0;		
		try{
			String str=String.valueOf(map.get("curPersonIds"));
			List<Object> list=JSON.parseArray(str);
			List< Map<String,Object>> listw = new ArrayList<Map<String,Object>>();
			for(Object object:list){
				Map<String,Object> ret=(Map<String, Object>) object;
				listw.add(ret);
			}
			int len=listw.size();
			curPersonIds=new Integer[len];	
			for(Map<String,Object> m:listw){						
				curPersonIds[count]=Integer.valueOf(String.valueOf(m.get("currentPersonID")));
				count++;
			}
			resultList=this.mapper.getErpUserForHrList(curPersonIds);
		}catch(Exception e){
			logger.error("getErpUserForHrList异常,异常信息为:"+e.getMessage(),e);
		}
		return RestUtils.returnSuccess(resultList);
	}
	
	/**
	 * @author songzixuan
	 * @date 2019-3-25
	 * @description 根据多个员工编号查erpuser表
	 */
	public RestResponse findErpUserByUserIdArray(String userId) {
		logger.info("findErpUserByUserIdArray方法开始执行,参数userId:"+userId);
		List<Map<String, Object>> erpUserList = null;
		try{
			erpUserList =  this.mapper.findErpUserByUserIdArray(userId);
		}catch(Exception e) {
			e.printStackTrace();
			logger.info("调用findErpUserByUserIdArray()方法出错"+e.getMessage(),e);
			return RestUtils.returnFailure("调用findErpUserByUserIdArray()方法出错"+e.getMessage());
		}
		
		return  RestUtils.returnSuccess(erpUserList);
	}	
}
