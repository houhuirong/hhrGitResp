package com.nantian.erp.hr.data.dao;

import com.nantian.erp.hr.data.model.ErpResumePostReexam;

/** 
 * Description: 复试信息表mapper
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
public interface ErpResumePostReexamMapper {

	//新增一条复试临时信息
	public void insertResumePostReexam(ErpResumePostReexam erpResumePostReexam);
	
	//删除一条复试临时信息
	public void deleteResumePostReexam(Integer interviewId);
	
	//修改一条复试临时信息
	public void updateResumePostReexam(ErpResumePostReexam erpResumePostReexam);
	
	//通过面试流程Id查询复试临时信息
	public ErpResumePostReexam selectResumePostReexamDatail(Integer interviewId);
}
