package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.ErpEmployeePostive;
import org.apache.ibatis.annotations.Param;

/**
 * 员工转正Mapper
 * @author ZhangYuWei
 */
public interface ErpEmployeePostiveMapper {

	List<Map<String, Object>> findAll(Map<String,Object> map);
	
	//add by 曹秀斌 查询所有转正
	List<Map<String, Object>> findAllPositive(Map<String,Object> map);
	
	//add 20180917  新增一条转正流程记录信息
	public void insertEmployeePostive(ErpEmployeePostive erpEmployeePostive);
	
	//add 20180917  根据ID修改一条转正流程记录信息，修改当前处理人
	public void updateEmployeePostive(ErpEmployeePostive erpEmployeePostive);
	
	//add 20180917  根据员工ID删除员工转正流程表一条记录
	public void deleteByEmployeeId(Integer employeeId);
	
	//add 20181029所有待转正
	List<Map<String,Object>> findAllNonPositive(Map<String,Object> map);

	//add 20181029 一级部门经理待处理
	List<Integer> findDmProcess(Integer userId);
	
	//超时未转正员工ID
	public List<Integer> findTimeoutNotPositionEmpId();

	//add 20181029所有待转正
	List<Map<String,Object>> findAllVpNonPositive(Integer[] userIds);

	//查询员工是否有待转正的数据
    List<ErpEmployeePostive> findPostiveByEmployeeId(Integer employeeId) throws Exception;

	void updateCurrentPersonIdById(@Param("id") Integer id, @Param("newsecdeptManager")Integer newsecdeptManager) throws Exception;

	List<Map<String, Object>> findErrorCurrentPersonPostive();
}
