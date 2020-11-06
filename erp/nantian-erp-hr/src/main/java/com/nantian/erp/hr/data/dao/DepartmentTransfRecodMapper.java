package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.DepartmentTransfRecod;
import org.apache.ibatis.annotations.Param;

/**
 * @author gaolp
 * 2019年3月20日
 * @version 1.0  
 */
public interface DepartmentTransfRecodMapper {

	//新增处理记录
	void insertTransfRecod(DepartmentTransfRecod transfrcode) throws Exception;
	//查询审批记录
	List<Map<String, Object>> findtransfRecode(Integer transferApplyID);
	//通过条件删除记录表
	public void deleteTransfRecode(Map<String, Object> param)throws Exception;

	void batchDeleteTransfRecode(@Param("transferApplyIdList") List<Integer> transferApplyIdList)throws Exception;
}
