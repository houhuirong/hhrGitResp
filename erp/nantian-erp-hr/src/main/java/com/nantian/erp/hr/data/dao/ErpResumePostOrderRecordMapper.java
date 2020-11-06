package com.nantian.erp.hr.data.dao;

import java.util.List;
import com.nantian.erp.hr.data.model.ErpResumePostOrderRecord;

/** 
 * Description: 面试预约记录mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月15日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpResumePostOrderRecordMapper {
	
	//新增一条面试预约记录
	public void insertResumePostOrderRecord(ErpResumePostOrderRecord resumePostOrderRecord);
	
	//通过面试流程Id查询全部面试预约记录
	public List<ErpResumePostOrderRecord> selectResumePostOrderRecord(Integer interviewId);
	
}
