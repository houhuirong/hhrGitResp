package com.nantian.erp.hr.data.dao;

import java.util.Map;

import com.nantian.erp.hr.data.model.PostTemplateRecord;

public interface PostTemplateRecordMapper {
	//根据模板id查询模板记录
	public Map<String,Object> findPostTemplateRecordById(Integer postTemplateId);
	//插入岗位模板记录
	public void addPostTemplateRecord(PostTemplateRecord postTemplateRecord);
	
}
