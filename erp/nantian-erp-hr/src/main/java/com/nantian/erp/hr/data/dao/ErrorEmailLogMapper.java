package com.nantian.erp.hr.data.dao;


import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.ErrorEmailLog;

/**
 * 邮件错误日志
 */
public interface ErrorEmailLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ErrorEmailLog record);
    
    int insertSelective(ErrorEmailLog record) throws  Exception;

    List<Map<String,Object>> selectByParam(Map<String,Object> map);
    
    int updateByPrimaryKeySelective(ErrorEmailLog record);

    List<Map<String,Object>> selectALL(Map<String,Object> map);

}