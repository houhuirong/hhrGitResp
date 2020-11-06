package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.DepartmentTransfApply;
import org.apache.ibatis.annotations.Param;

/**
 * @author gaolp
 * 2019年3月15日
 * @version 1.0  
 */
public interface DepartmentTransfApplyMapper {

	//新增工作调整申请
	public Integer addDepTransfApply(DepartmentTransfApply gropTransfapply) throws  Exception;

	//查询待我处理的申请
	public List<Map<String, Object>> findWaitMsgById(Map<String,Object> params);
	//改变申请状态-批准
	void updataTransfStatus(DepartmentTransfApply gropTransfapply) throws Exception;
	
	//查询所有的工作调动申请
	List<Map<String,Object>> findAllTransfApply(Map<String,Object> params);
	//根据登录人员的id查询其所有申请以及审批状态
	public List<Map<String, Object>> findtransfRecodeBytoken(Integer employeeId);
	//根据员工id和申请id查询单条申请，用于修改
	public Map<String, Object> findtransfRecodeById(Integer id) throws Exception;
	//删除部门调整申请
	void deleteTransferApply(Integer id)throws Exception;
	//根据生效时间查询是否已提交同时间的部门调动申请
	public List<Map<String, Object>> findtransfRecodeByStartTime(Integer employeeId) throws Exception;
	
	/* *********************************** 定时器  *********************************** */
	//查询审批通过的人以及生效时间=当天时间（用于定时器）
	List<Map<String, Object>> selectMsgForschedule();

	/**
	 * 根据登录人查询本人申请的部门调动列表
	 * @param params
	 * @return
	 */
	List<Map<String, Object>> queryDepartmentTransfApplyList(Map<String, Object> params);

	/**
	 * 根据id查询部门调整信息
	 * @param id
	 * @return
	 */
	DepartmentTransfApply findtransfInfoById(Integer id) throws Exception;

	Map<String, Object> queryDepartmentTransfApplyInfo(Integer id);

	/**
	 * 批量删除部门调整
	 * @param idList
	 */
	void batchDeleteTransferApply(@Param("idList") List<Integer> idList) throws Exception;
}
