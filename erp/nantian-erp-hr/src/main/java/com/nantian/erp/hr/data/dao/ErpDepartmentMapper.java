package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

import com.nantian.erp.hr.data.model.ErpDepartment;

/**
 * 部门（组织架构、员工信息）Mapper
 * @author ZhangYuWei
 */
public interface ErpDepartmentMapper {
	
	//插入部门信息
	public void insertDepartment2(ErpDepartment department);
	
	//删除部门信息
	public void deleteDepartment(Integer departmentId);
	
	//修改部门信息
	public void updateDepartment(ErpDepartment department);
	
	//根据部门id查询员工信息表的记录数（删除前的判断）
	public List<Map<String,Object>> findEmployeeList(Integer departmentId);
	
	//查询全部部门信息
	public List<ErpDepartment> findAllDepartment();
	
	//根据ID查询部门信息
	public ErpDepartment findByDepartmentId(Integer departmentId) throws Exception;
	
	//add by ZhangYuWei 查询所有一级部门
	List<Map<String,Object>> findAllFirstDepartment();
	
	//add by ZhangYuWei 条件查询全部一级部门
	List<Map<String,Object>> findAllFirstDepartmentByParams(Map<String,Object> params);
		
	//add by ZhangYuWei 根据一级部门ID查询全部二级部门信息
	List<Map<String,Object>> findAllSecondDepartmentByFirDepId(Integer departmentId);
	
	//根据一级部门经理的邮箱查询部门信息
	public List<ErpDepartment> findByDepartmentManagerEmail(String departmentManagerEmail);
	
	//根据一级部门名字获取一级部门ID  add by cxb 
	Integer selectIdByFirstDepartmentName(String firstDepartmentName);
	
	//根据二级部门名字获取二级部门ID  add by cxb 
	public Integer selectIdBySecondDepartmentName(Map<String, Object> paramDepartName);
	
	//根据上级领导ID查询所有一级部门经理ID
	public List<Map<String,Object>> findFirDepIdBySuperLeader(Map<String, Object> map);
	
	//根据userId 查找一级部门名称 
	List<ErpDepartment> findDepartmentByUserID(Map<String, Object> map);

	public List<Map<String, Object>> findDepMessBySLeader(Map<String, Object> mapL);

	public List<Map<String, Object>> findSecondDepByFirstDep(Integer departmentId);

	public List<Map<String, Object>> findSDepMessByDepartmentID(Integer departmentId);
	
	//根据条件查询部门人数
	Integer countEmployeeNumByParams(Map<String,Object> params);
	
	//查询部门的开始时间
	public String getDepartmentStartTime(Map<String,Object> params);
	
	//查询当月员工总数
	public Integer coutEmployeeNumByMonth(Map<String,Object> params);
	
	//查询当月部门入职员工总数
	public Integer coutEntryNumByMonth(Map<String,Object> params);
	
	//查询当月部门离职员工总数
	public Integer coutDimissionNumByMonth(Map<String,Object> params);
	
	//查询当月换部门进入员工总数
	public Integer coutChangeInNumByMonth(Map<String,Object> params);
	
	//查询当月换部门离开员工总数
	public Integer coutChangeOutNumByMonth(Map<String,Object> params);
	
	//插入员工和部门的关联
	public void insertEmpDepRelation(Map<String,Object> params);
	
	//更新员工和部门的关联
	public void updateEmpDepRelation(Map<String,Object> params);
	
	//更新员工和部门的关联表的部门名称
	public void updateDepartmentName(Integer departmentId);
	
	//查询员工与部门的关联
	public List<Map<String, Object>> getEmpDepRelation(Map<String,Object> params);
	
	//查询所有一级部门
	public List<Map<String,Object>> findFirstDepartments();
	
	//根据上级领导查看一级部门信息
	public List<Map<String,Object>> findFirstDepBySuperById(Integer employeeId);
	
	//修改部门信息
	public void invalidDepartment(Map<String,Object> params);
	
	//查询符合条件的一级部门信息
	public List<Map<String,Object>> findLoginUserDepartmentByParams(Map<String,Object> params);
	
	//查询符合条件的二级部门信息及其对应的一级部门信息
	public List<Map<String,Object>> findLoginUserSecondDeptByParams(Map<String,Object> params);

	/**
	 * 查询部门的兄弟部门列表
	 * @param departmentId
	 * @return
	 */
    List<Map<String, Object>> findBrotherDepartmentList(Integer departmentId);

    ErpDepartment findByDepartmentIdAndValid(Integer upperDepartment);

	List<Map<String, Object>> findSecondDepartmentByFirstDepartmentId(Integer departmentId);

	/**
	 * 根据登录人查看所管理的一级部门及上级领导列表
	 * @param params
	 * @return
	 */
    List<Map<String, Object>> findFirstDepartmentAndSuperLeaderByUserId(Map<String, Object> params);

	List<Map<String, Object>> findAllSecondDepartmentBySupperId(Integer departmentId);
	//条件查询二级部门管辖所有部门
	List<Map<String,Object>> findAllFirstDepartmentBySecDep(Map<String,Object> params);

	/**
	 * 查询权限的二级部门
	 * @param params
	 * @return
	 */
	List<Map<String, Object>> findSecondDepartmentByPowerParams(Map<String, Object> params);
}

