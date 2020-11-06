/**
 * 
 */
package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.salary.data.model.ErpActualInsuranceFundBase;

/**
 * Description: 员工薪酬-员工实际的社保和公积金的基数
 * 
 * @author 宋修功
 * @date 2020年02月18日
 * @version 1.0
 */
public interface ErpActualInsuranceFundBaseMapper {
	// 新增一条员工实际的社保和公积金的基数
	public void insertActualInsuranceFundBase(ErpActualInsuranceFundBase erpActualInsuranceFundBase);

	// 按照员工ID查找社保和公积金的基数【每一个员工只有一条社保和公积金的基数数据】
	public ErpActualInsuranceFundBase selectActualInsuranceFundBaseByEmployeeID(Map<String,Object> employeeID);

	//根據更新社保和公积金的基数
	public void updateActualInsuranceFundBase(ErpActualInsuranceFundBase erpActualInsuranceFundBase);

	// 跟据部门ID、员工Name进行分页查询员工实际的社保和公积金的基数
	public List<ErpActualInsuranceFundBase> selectActualInsuranceFundBaseByParameters(Map<String,Object> params);
	
	//根据员工ID和有效月份查询每月实际社保基数和公积金基数
	public ErpActualInsuranceFundBase findActualSocialFundByIdAndMonth(Map<String,Object> map);
}
