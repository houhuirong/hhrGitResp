package com.nantian.erp.hr.data.dao;

import java.util.Map;

import com.nantian.erp.hr.data.model.ErpResumePostOrder;

/** 
 * Description: 面试预约信息mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月07日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpResumePostOrderMapper {
	
	//新增一条面试预约信息
	public void insertResumePostOrder(ErpResumePostOrder erpResumePostOrder);
	
	//删除一条面试预约信息
	public void deleteResumePostOrder(Integer interviewId);
	
	//修改一条面试预约信息
	public void updateResumePostOrder(ErpResumePostOrder erpResumePostOrder);
	
	//通过面试流程Id查询面试预约信息
	public Map<String,Object> selectResumePostOrderDetail(Integer interviewId);
	
}
