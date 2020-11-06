package com.nantian.erp.salary.data.dao;

import com.nantian.erp.salary.data.model.DepartmentSalaryAdjust;
import com.nantian.erp.salary.data.vo.DepartmentSalaryAdjustVO;

import java.util.List;
import java.util.Map;

public interface DepartmentSalaryAdjustMapper {
    /**
     * 根据条件查询调薪批次
     * @param paramMap
     * @return
     */
    List<DepartmentSalaryAdjustVO> findDepartmentSalaryAdjustList(Map<String, Object> paramMap);

    /**
     * 新增一级部门调薪批次
     * @param departmentSalaryAdjust
     */
    void insertDepartmentSalaryAdjust(DepartmentSalaryAdjust departmentSalaryAdjust) throws Exception;

    /**
     * 更新一级部门调薪批次
     * @param departmentSalaryAdjust
     */
    void updateDepartmentSalaryAdjust(DepartmentSalaryAdjust departmentSalaryAdjust) throws Exception;

    /**
     * 根据主键查询调薪批次信息
     * @param id
     * @return
     */
    DepartmentSalaryAdjust selectByPrimaryKey(Integer id);

    int deleteByPrimaryKey(Integer id) throws Exception;

}
