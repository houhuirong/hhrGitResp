package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpPayRollFlow;

import org.apache.ibatis.annotations.Param;

/** 
 * Description: 上岗工资单流程审批mapper
 *
 * @author HouHuiRong
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月06日      		HouHuiRong          1.0       
 * </pre>
 */
public interface ErpPayRollFlowMapper {
	
	//新增上岗工资单流程审批数据
	void insertPayRollFlow(Map<String,Object> map);
	//查询当前处理人未审批的员工IDs

	List<Map<String,Object>>  findUserIdsByCurrentPerID(Map<String,Object> map);

	//更新status
	void updatePayRollFlow(ErpPayRollFlow erpPayRollFlow);
	
	//查询入职所有上岗工资单
	List<Map<String,Object>> findAllPeriodPayRoll(Map<String,Object> map);
	//查询转正所有上岗工资单
	List<Map<String,Object>> findAllPositivePayRoll(Map<String,Object> map);
	//查询未处理的转正工资
	List<Map<String,Object>> findAllPositiveWaitapprove(Map<String,Object> map);
	//批量查询试用期未处理和已处理工资单
	List<Map<String, Object>> findAllPeriodPayRollForSuper(Integer[] curPersonIds);
	//批量查询转正未处理和已处理工资单
	List<Map<String, Object>> findAllPositivePayRollForSuper(Integer[] curPersonIDs);
	
	// add by ZhangYuWei 20190611 通过员工ID查询上岗工资单
	ErpPayRollFlow findPeriodPayRollByEmpId(Integer employeeId);

	// add by ZhangYuWei 20190612 查询社保基数的提交月份
	List<ErpPayRollFlow> findAllPayRollSecMonth(Map<String, Object> map);

	/**
	 * 查询待处理的上岗工资单、转正工资单
	 * @param employeeId
	 * @return
	 */
	List<ErpPayRollFlow> findWaitPayRollFlowByUserId(Integer employeeId) throws Exception;

	void updateCurrentPersonIdById(@Param("id") Integer id, @Param("newFirstManager") Integer newFirstManager) throws Exception;

	List<Map<String, Object>> findAllConfirmPositivePayRoll(Map<String, Object> query);

	List<ErpPayRollFlow> findWaitPayRollFlowList() throws Exception;
	boolean findIsConfirmedByUserId(@Param("userId") Integer userId);
}
