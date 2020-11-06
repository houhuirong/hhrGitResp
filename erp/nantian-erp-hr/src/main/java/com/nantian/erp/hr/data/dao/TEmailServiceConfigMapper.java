package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.TEmailServiceConfig;

public interface TEmailServiceConfigMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TEmailServiceConfig record);

    List<Map<String,Object>> selectByParam(Map<String,Object> map);

    int updateByPrimaryKeySelective(TEmailServiceConfig record);
}