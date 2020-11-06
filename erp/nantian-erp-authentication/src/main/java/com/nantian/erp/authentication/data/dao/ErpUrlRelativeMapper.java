package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;

public interface ErpUrlRelativeMapper{
	
	void insertUrlRelative(Map map);
	
	List<Map<String,Object>> selectUrlRelativeByUrlId(Integer urlId);
	
	List<Map<String,Object>> selectUrlRelativeByRelativeId(Integer relativeType, Integer relativId);
	
	void deleteUrlRelativeByID(Integer id);
	
	void deleteUrlRelative(Map map);
}