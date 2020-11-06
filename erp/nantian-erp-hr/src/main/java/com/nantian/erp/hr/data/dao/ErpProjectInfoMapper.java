package com.nantian.erp.hr.data.dao;

import java.util.List;

import com.nantian.erp.hr.data.model.ErpProjectInfo;

/**
 * 项目信息Mapper
 * @author ZhangYuWei
 */
public interface ErpProjectInfoMapper {
	
	//查询全部项目信息列表
	public List<ErpProjectInfo> findAllProjectInfo();
	
}
