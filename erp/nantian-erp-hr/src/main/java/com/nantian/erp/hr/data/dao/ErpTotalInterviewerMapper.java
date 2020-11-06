package com.nantian.erp.hr.data.dao;

import com.nantian.erp.hr.data.model.ErpTotalInterviewer;

/**
 * 员工面试总记录Mapper
 * @author ZhangYuWei
 */
public interface ErpTotalInterviewerMapper {
	
	//新增面试记录（统计用，此表不删除数据）
	public void insertTotalInterviewer(ErpTotalInterviewer interviewer);
	
	//查询面试记录总数
	public int countInterviewer(Integer postId);
	
}
