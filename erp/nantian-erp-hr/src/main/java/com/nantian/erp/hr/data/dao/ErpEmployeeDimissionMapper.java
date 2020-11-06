package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.ErpDimission;

/** 
 * Description: 员工离职mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月17日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpEmployeeDimissionMapper {

	//增加一条离职员工信息
	void insertDimission(ErpDimission dimission);
	
	//通过离职表ID删除一条离职员工信息
	void deleteDimission(Integer dimissionId);
	
	//修改一条离职员工信息
	void updateDimission(ErpDimission dimission);
	
	//查询一个员工的信息
	Map<String, Object> selectEmployeeDetail(Integer employeeId);
	
	//条件查询离职申请记录信息
	List<Map<String, Object>> selectEmployeeAllDimissionByParams(Map<String, Object> params);
	
	//查询离职申请记录信息总数
	Long findTotalCountOfDimissionApply(Map<String, Object> params);	
	
	//通过离职年月查询员工总数
	public Integer findCountByDimissionTime(Map<String, Object> params);
	
	//查询员工的离职信息
	ErpDimission findOneById(Integer employeeId);
	
}
