package com.nantian.erp.hr.data.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.ErpEmployeeEntry;
import org.apache.ibatis.annotations.Param;

/** 
 * Description: 员工入职Mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月14日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpEmployeeEntryMapper {
	
	//查询一个岗位的已入职人数
	public Integer selectCountAllEntry(Integer postId);

//	Map<String, Object> findAll(Integer offerId);

	List<Map<String, Object>> findByRole(HashMap<String, Object> queryHashMap);
	
	// add by caoxb  查询入职 列表  -导出专用 
//	List<Map<String, Object>> findAllEntry(Map<String,Object> map);
	
	//add 20180914  新增一条入职流程记录
	public void insertEmployeeEntry(ErpEmployeeEntry employeeEntry);
	
	//add 20180917  通过offerID修改当前处理人
	public void updateEmployeeEntryByOfferId(ErpEmployeeEntry employeeEntry);
	
	//add 20180917  通过offerID删除员工入职流程记录信息
	public void deleteByOfferId(Integer offerId);
	
	//查询所有待我处理列表
	List<Map<String,Object>> findAllWaitingForMe(Map<String,Object> map);
	
//	//一级部门经理所有待入职
//	Map<String, Object> findAllDmNonEntry(Integer offerId);
	//所有待入职
	List<Map<String,Object>> findAllWaitingEntry(Map<String,Object> map);
	
	//通过部门经理编号查询部门经理姓名
	String findEmpNameByEmpId(Integer employeeId);	
//	//查询所有待我处理列表
//	List<Integer> findAllWaitingForDm(Map<String,Object> map);
	
//	//查询所有待我处理列表
//    List<Integer> findAllWaitingEntry();
    
    //查询hr角色ID
    List<Integer> findRoleId();
//    
//    List<Integer> findAllWaitingToMe(Map<String,Object> map);

	public List<Map<String,Object>> findAllWaitingToDm();

	List<Integer> findIdsByOfferIds(@Param("offerIds") List<Integer> offerIds) throws Exception;

	void updateValidFalseByEntryIds(@Param("entryIds")List<Integer> entryIds) throws Exception;
	//所有入職待處理中項目未分配
	List<Map<String,Object>> findAllEntried(Map<String,Object> map);
}
