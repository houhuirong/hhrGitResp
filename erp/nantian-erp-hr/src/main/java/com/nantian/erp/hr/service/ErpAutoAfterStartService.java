package com.nantian.erp.hr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeDimissionMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.model.ErpDepartment;
import com.nantian.erp.hr.data.model.ErpDimission;

/** 
 *
 * Description: 服务启动时，将员工信息同步到Redis中(Service)
 * @author ZhangYuWei
 * @create 2019-05-16
 */
@Service
public class ErpAutoAfterStartService {

	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ErpEmployeeMapper erpEmployeeMapper;
	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpEmployeeDimissionMapper employeeDimissionMapper;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Description：将员工信息、部门信息写入redis，供查询
	 * @author ZhangYuWei
	 * @create 2019-05-16
	 */
	@SuppressWarnings("unchecked")
	public void saveEmployeeAndDepartmentToRedis() {
		logger.info("saveEmployeeAndDepartmentToRedis方法开始执行，传递参数：无");
		//查询全部的员工信息，员工信息（员工id、姓名、性别、一级部门id、二级部门id、状态）写入redis，供查询
		List<Map<String,Object>> employeeList = erpEmployeeMapper.findEmployeeAll();
		for (Map<String, Object> employee : employeeList) {
			Integer employeeId = (Integer) employee.get("employeeId");//员工Id
			String employeeName = (String) employee.get("name");//员工姓名
			String sex = (String) employee.get("sex");//性别
			Integer firstDepartmentId = (Integer) employee.get("firstDepartmentId");//一级部门id
			Integer secondDepartmentId = (Integer) employee.get("secondDepartmentId");//二级部门id
			String status = (String) employee.get("status");//状态
			String statusName = (String) employee.get("statusName");//状态名
			String position = (String) employee.get("position");//职位
			String dimissionTime = (String) employee.get("dimissionTime");//离职时间
			
			Map<String,Object> employeeMap = new HashMap<>();//员工基本信息
			employeeMap.put("employeeId", employeeId);
			employeeMap.put("employeeName", employeeName);
			employeeMap.put("sex", sex);
			employeeMap.put("firstDepartmentId", firstDepartmentId);
			employeeMap.put("secondDepartmentId", secondDepartmentId);
			employeeMap.put("status", status);
			employeeMap.put("statusName", statusName);
			employeeMap.put("position", position);
			employeeMap.put("dimissionTime", dimissionTime);
			
			redisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_EMPLOYEE+employeeId, employeeMap);
		}
		
		//查询全部的部门信息，部门信息（部门id、部门名、部门经理、上级领导）写入redis，供查询
		List<ErpDepartment> departmentList = erpDepartmentMapper.findAllDepartment();
		for (ErpDepartment department : departmentList) {
			Integer departmentId = department.getDepartmentId();//部门id
			String departmentName = department.getDepartmentName();//部门名
			Integer userId = department.getUserId();//部门经理
			Integer superLeader = department.getSuperLeader();//上级领导
			
			Map<String,Object> departmentMap = new HashMap<>();//部门基本信息
			departmentMap.put("departmentId", departmentId);
			departmentMap.put("departmentName", departmentName);
			departmentMap.put("userId", userId);
			departmentMap.put("superLeader", superLeader);
			redisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_DEPARTMENT+departmentId, departmentMap);
		}
		
		//getEmployeeAndDepartmentFromRedis(22,22);// 测试功能
	}
	
	/**
	 * Description：从redis中获取员工信息、部门信息（测试方法）
	 * @author ZhangYuWei
	 * @create 2019-06-04
	 */
	@SuppressWarnings("unchecked")
	public void getEmployeeAndDepartmentFromRedis(Integer employeeId,Integer departmentId) {
		logger.info("getEmployeeAndDepartmentToRedis方法开始执行，传递参数：无");
		Map<String, Object> ErpEmployee = (Map<String, Object>) redisTemplate.opsForValue()
				.get(DicConstants.REDIS_PREFIX_EMPLOYEE + employeeId);
		Map<String, Object> ErpDepartment = (Map<String, Object>) redisTemplate.opsForValue()
				.get(DicConstants.REDIS_PREFIX_DEPARTMENT + departmentId);
		logger.info("员工信息："+ErpEmployee);
		logger.info("部门信息："+ErpDepartment);
	}
	
}
