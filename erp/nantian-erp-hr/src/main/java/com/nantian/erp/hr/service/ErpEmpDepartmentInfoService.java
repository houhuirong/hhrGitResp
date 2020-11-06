package com.nantian.erp.hr.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import com.nantian.erp.hr.data.dao.ErpEmpDepartmentInfoMapper;

/**
 * service 层
 * 功能：用于其他服务关于员工-部门信息的接口调用
 * @author caoxb
 * @date 2018年09月08日
 */
@Service
@Configuration
public class ErpEmpDepartmentInfoService {

	@Autowired
	private ErpEmpDepartmentInfoMapper erpMonthPerformanceMapper;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/*
	 * 根据部门一级部门ID查询一级部门名字
	 * 参数：null
	 */
	public Map<String, Object> getFirstDepartment(Integer departmentId) {
		logger.info("getFirstDepartment方法开始执行，传递参数：departmentId:" + departmentId);

		return this.erpMonthPerformanceMapper.getFirstDepartment(departmentId);
	}
	
	/*
	 * 根据员工ID查询其名字及所属二级部门
	 * 参数：null
	 */
	public Map<String, Object>  getErpEmpAndSecondDepart(Integer erpEmployeeId) {
		logger.info("getErpEmpAndSecondDepart方法开始执行，传递参数：erpEmployeeId:" + erpEmployeeId);

		return this.erpMonthPerformanceMapper.getErpEmpAndSecondDepart(erpEmployeeId);
	}
	
	/*
	 * 根据简历ID查询岗位信息
	 * 参数：null
	 */
	public Map<String, Object> findPostInfo(Integer offerId) {
		logger.info("findPostInfo方法开始执行，传递参数：offerId:" + offerId);

		//岗位相关信息
		Map<String, Object> postInfo = null;
		try {
			postInfo = this.erpMonthPerformanceMapper.findPostInfo(offerId);
		} catch (Exception e) {
			logger.error("findPostInfo方法出现异常：" + e.getMessage(),e);
		}
		return postInfo;
	}
	
	
	/*
	 * 入职试用期-上岗工资单员工及其部门信息
	 * 参数：token
	 */
	public List<Map<String, Object>> findAllHasHiredInfo(String userName) {
		logger.info("findAllHasHiredInfo方法开始执行，传递参数：userName:" + userName);

		//根据当前登陆人查询所有已入职的员工及部门信息
		List<Map<String, Object>> listInfo = null;
		try {
			listInfo = this.erpMonthPerformanceMapper.selectAllHasHiredByUserName(userName);
		} catch (Exception e) {
			logger.error("findAllHasHiredInfo方法出现异常：" + e.getMessage(),e);
		}
		return listInfo;
	}
	
	/*
	 * 入职转正-上岗工资单-员工及其部门信息
	 * 参数：token
	 */
	public List<Map<String, Object>> findAllPositiveInfo(String userName) {
		logger.info("findAllPositiveInfo方法开始执行，传递参数：userName:" + userName);

		//根据当前登陆人查询所有已入职的员工及部门信息
		List<Map<String, Object>> listInfo = null;
		try {
//			listInfo = this.erpMonthPerformanceMapper.selectAllPositiveByUserName(userName);
//			for (Map<String, Object> map : listInfo) {
//				//查询岗位信息
//				Integer resumeId = Integer.valueOf(String.valueOf(map.get("resumeId")));
//				Map<String, Object> tempMap = this.erpMonthPerformanceMapper.findPostInfo(resumeId);
//				map.put("postName", String.valueOf(tempMap.get("postName")));
//			}
		} catch (Exception e) {
			logger.error("findAllPositiveInfo方法出现异常：" + e.getMessage(),e);
		}
		return listInfo;
	}
	
	/*
	 * 根据员工ID查询员工及其部门-岗位信息
	 * 参数：employeeId
	 */
	public Map<String, Object> findEmpInfo(Integer employeeId) {
		logger.info("findEmpInfo方法开始执行，传递参数：employeeId:" + employeeId);

		//根据当前登陆人查询所有已入职的员工及部门信息
		Map<String, Object> mapInfo = null;
		try {
			mapInfo = this.erpMonthPerformanceMapper.selectEmpInfo(employeeId);
		} catch (Exception e) {
			logger.error("findEmpInfo方法出现异常：" + e.getMessage(),e);
		}
		return mapInfo;
	}
	
	
	
}
