package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.salary.data.model.ErpSalaryMonthPerformance;

/** 
 * Description: 月度绩效mapper
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年12月02日                      ZhangYuWei          1.0        
 * </pre>
 */
public interface ErpSalaryMonthPerformanceMapper {
	
	//创建月度绩效
	void createErpMonthPerformance(ErpSalaryMonthPerformance erpSalaryMonthPerformance);
	
	//主键修改月度绩效
	void updateErpMonthPerformance(ErpSalaryMonthPerformance erpSalaryMonthPerformance);
	
	//锁定时更新月度绩效数据
	void updateErpMonthPerformanceById(ErpSalaryMonthPerformance erpSalaryMonthPerformance);
	
	//根据一级部门ID、月份修改月度绩效
	void updateErpMonthPerformanceByParam(ErpSalaryMonthPerformance erpSalaryMonthPerformance);
	
	//查询一个员工的月度绩效（条件：员工Id、月度）
	ErpSalaryMonthPerformance findEmpMonthPerformanceDetail(Map<String, Object> param);
	
	//查询多个员工的月度绩效（条件：一级部门Id、月度）（暂未用到）
	List<ErpSalaryMonthPerformance> findEmpMonthPerformanceMore(Map<String, Object> param);
	
	//查询一级部门的月度绩效状态
	String findFirstDepartmentMonthPerStatus(Map<String, Object> param);
	
	//新增月度绩效状态表
	void insertFirstDepartmentMonthPerStatus(Map<String, Object> param);
	
	//更新月度绩效状态表
	void updateFirstDepartmentMonthPerStatus(Map<String, Object> param);
	
	//删除绩效数据
	void deleteMonthPerformance(Map<String, Object> param);
	
	//删除绩效数据
	void deleteMonthPerformanceApply(Map<String, Object> param);
	
	//按照参数查询月度状态数据
	List<Map<String,Object>> findMonthPerformanceApplyByParams(Map<String,Object> params);

	//获取指定月份的所有状态为提交月度绩效及绩效状态
	List<Map<String,Object>> searcMonthPerformanceAndStatusByParams(Map<String,Object> params);
	
	List<String> findFirstDepartmentMonthPerStatusList(@Param("departmentIdList")List<Integer> departmentIdList,@Param("startTime")String startTime,@Param("endTime")String endTime);
}
