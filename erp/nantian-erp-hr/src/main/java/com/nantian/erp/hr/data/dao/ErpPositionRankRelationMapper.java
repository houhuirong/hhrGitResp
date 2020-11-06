package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.ErpPositionRankRelation;

/**
 * 职位职级关联Mapper
 * @author lixu
 */
public interface ErpPositionRankRelationMapper {
	//通过职位名称查找职位编号没有值返回0
	public int selectPostionNoByPostName(String postionName);
	
	//根据职位编号查找职位职级关联表对象com.nantian.erp.hr.data.model
	public  ErpPositionRankRelation selectErpPositionRankRelationByPostionNo(Integer positionNo);
	
	//add by ZhangYuWei 20181110  根据职位类别、职位子类、职位族类查询职位名称、职级列表
	public List<Map<String,Object>> selectPositionRankList(Map<String,Object> params);
	
	//通过职位名称查找职位编号没有值返回0
	public  ErpPositionRankRelation  selectPostionNoByPostNameForSaveBug(String postionName);
	
}
