package com.nantian.erp.salary.data.dao;


import com.nantian.erp.salary.data.model.DepartmentSalaryAdjustApproveRecord;

public interface DepartmentSalaryAdjustApproveRecordMapper {
    int deleteByPrimaryKey(Integer id)throws Exception;

    int insert(DepartmentSalaryAdjustApproveRecord record)throws Exception;

    int insertSelective(DepartmentSalaryAdjustApproveRecord record) throws Exception;

    DepartmentSalaryAdjustApproveRecord selectByPrimaryKey(Integer id)throws Exception;

    int updateByPrimaryKeySelective(DepartmentSalaryAdjustApproveRecord record)throws Exception;

    int updateByPrimaryKey(DepartmentSalaryAdjustApproveRecord record)throws Exception;
}