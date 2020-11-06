package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.*;
import org.apache.ibatis.annotations.Param;


/**
 * 薪资调整
 * @author hhr
 * @date 2018-9-09
 * */
public interface ErpSalaryAdjustMapper{
	
	//查询未审批员工IDs
	List<Map<String,Object>> findUnapproveByEmpId(@Param(value="employeeId") Integer employeeId);
	
	//创建薪资调整流程数据
	boolean createErpSalaryAdjustFlow(ErpSalaryAdjustFlow erpSalaryAdjustFlow);
	
	//创建薪资调整审批记录
	boolean createErpSalaryAdjustApproveRecord(ErpSalaryAdjustApproveRecord erpSalaryAdjustApproveRecord);
	
	//查询薪资调整申请所有未审批员工ID
	List<Integer> findUnapproveEmpIdsInFlow();
	
	//查询转正员工薪资调整所有待我处理
	List<Map<String, Object>> findAllPositiveSalAdjusIndividualtApproval(@Param(value="employeeId") Integer employeeId);
	
	//查询转正员工薪资调整所有待我处理
	List<Map<String, Object>> findAllPeriodSalAdjusIndividualtApproval(@Param(value="employeeId") Integer employeeId);
	
	//通过流程表ID查询审批记录
	List<Map<String,Object>> findApproveRecordByFlowId(@Param(value="salaryAdjustFlowId") Integer salaryAdjustFlowId);
	
	//更新薪资调整流程类型为已生效
	boolean updateSalAdjustFlowOfType(@Param(value="salaryAdjustFlowId") Integer salaryAdjustFlowId);
	
	//更新薪资调整流程类型为待生效
	boolean updateSalAdjustFlowType(@Param(value="salaryAdjustFlowId") Integer salaryAdjustFlow);	
	
	//根据员工ID更新转正工资单
	int updatePositivePayrollByEmpId(ErpPositivePayroll erpPositivePayroll);
	
	//根据员工ID修改试用期工资
	int updatePeriodPayrollByEmpId(ErpPeriodPayroll erpPeriodPayroll);
	
	//在流程表中查询已审批员工IDs
	List<Integer> findApprovedEmpIdsInFlow();
	
	//通过员工ID查询流程表ID
	List<Integer> findFlowIdByEmployeeId(@Param(value="employeeId") Integer employeeId);
	
	//查询所有薪资申请
	List<Map<String, Object>> findAllPositionSalAdjustApproval(@Param(value="employeeId") Integer employeeId);
	
	//查询所有薪资申请
	List<Map<String, Object>> findAllPeriodSalAdjustApproval(@Param(value="employeeId") Integer employeeId);
	
	//通过员工ID查询审批记录
	List<Map<String,Object>> findApproveRecordByEmpId(@Param(value="employeeId") Integer employeeId);

	boolean insertSalaryMess(Map<String, Object> oneDepOnePeopMessMap);

	Map<String,Object> selectSarAdjByEId(Integer id);

	List<Map<String,Object>> selectSalAdjRecByEId(Integer id);

	boolean updateSalaryMess(Map<String, Object> map);

	Map<String, Object> selectSarAdj2ByEId(@Param("employeeId") Integer employeeId, @Param("year") String year);

	boolean updateStatueMess(Integer employeeId);

	Map<String, Object> selectSarAdj3ByEId(@Param("employeeId") Integer employeeId, @Param("year") String year);
	
	//add by ZhangYuWei 20190220  根据调整时间查询调薪计划
	List<Map<String,Object>> selectSalAdjRecByAdjustTime(String adjustTime);

	/**
	 * 查询员工调薪列表
	 * @param employeeParamMap  status 状态  firstDepartmentId 一级部门ID  employeeIdList员工列表
	 * @return
	 */
    List<Map<String, Object>> findEmployeeSalaryAdjustListByparams(Map<String, Object> employeeParamMap);

	/**
	 * 获取各员工上次调整时间
	 * @return
	 */
	List<Map<String, Object>> findEmployeeLastSalaryAdjustList();

	/**
	 * 新增调薪记录
	 * @param salaryAdjustRecord
	 */
    void insertSelective(SalaryAdjustRecord salaryAdjustRecord) throws Exception;

	/**
	 * 更新调薪记录
	 * @param salaryAdjustRecord
	 */
	void updateByPrimaryKeySelective(SalaryAdjustRecord salaryAdjustRecord) throws Exception;

	/**
	 * 删除调薪记录
	 * @param id
	 */
	void deleteByPrimaryKey(Integer id) throws Exception;

	/**
	 * 查询调薪记录详情
	 * @param id
	 */
	SalaryAdjustRecord selectByPrimaryKey(Integer id) throws Exception;

	/**
	 * 根据调薪id调薪记录
	 * @param id
	 * @return
	 */
	List<SalaryAdjustRecord> findSalaryAdjustReCordById(Integer id)throws Exception;

	/**
	 * 根据员工查询调薪记录
	 * @param employeeId
	 * @return
	 */
    List<SalaryAdjustRecord> findSalaryAdjustReCordByEmployeeId(Integer employeeId)throws Exception;

	/**
	 * 批量审批驳回
	 * @param salaryAdjustWaitIdList
	 */
	void updateRejectByIds(@Param("salaryAdjustWaitIdList")  List<Integer> salaryAdjustWaitIdList, @Param("approverId") Integer approverId)throws Exception;

	/**
	 * 查询该员工是否有其他未审批的调薪
	 * @param employeeId
	 */
	List<Integer> findSalaryAdjustReCordByEmployeeIdAndNotApprover(Integer employeeId) throws Exception;

	/**
	 * 查询该员工待处理的调薪数据
	 * @param employeeId
	 * @return
	 * @throws Exception
	 */
	List<SalaryAdjustRecord> findWaitSalaryAdjustByEmployeeId(Integer employeeId)throws Exception;

	void updateApproverIdById(@Param("id")Integer id, @Param("newFirstDepartmentSuperLeader")Integer newFirstDepartmentSuperLeader)throws Exception;

	/**
	 * 查询待处理的调薪申请
	 * @return
	 * @throws Exception
	 */
	List<SalaryAdjustRecord> findWaitSalaryAdjustList()throws Exception;
}
