package com.nantian.erp.salary.data.dao;

import java.util.List;
import com.nantian.erp.salary.data.model.ErpSalaryRangeSet;

/** 
 * Description: 薪酬范围设置mapper
 *
 * @author HouHuiRong
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年1月17日      		HouHuiRong          1.0       
 * </pre>
 */
public interface ErpSalaryRangeSetMapper {
	//根据职级查询薪资范围设置
	ErpSalaryRangeSet findSalaryRangeSetByRank(Integer rank);
	//查询所有薪资范围设置
	List<ErpSalaryRangeSet> findAllSalaryRangeSet();	
	//新增职级薪资范围设置
	void insertSalaryRangeSet(ErpSalaryRangeSet erpSalaryRangeSet);		
	//修改职级薪资范围设置
	void updateSalaryRangeSet(ErpSalaryRangeSet erpSalaryRangeSet);
	//根据职级删除薪资范围设置
	void deleteSalaryRangeSet(Integer salaryRangeSetId);

}
