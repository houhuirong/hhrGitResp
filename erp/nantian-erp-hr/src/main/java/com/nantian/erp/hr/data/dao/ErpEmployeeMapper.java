package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import com.nantian.erp.hr.data.model.ErpCertificate;
import com.nantian.erp.hr.data.model.ErpEducationExperience;
import com.nantian.erp.hr.data.model.ErpEmployee;
import com.nantian.erp.hr.data.model.ErpProjectExperience;
import com.nantian.erp.hr.data.model.ErpWorkExperience;
import com.nantian.erp.hr.data.vo.EmployeeQueryByDeptUserVo;
import com.nantian.erp.hr.data.vo.ExpenseReimbursementVo;
import com.nantian.erp.hr.data.model.ErpTechnicaExpertise;

/**
 * 员工信息Mapper
 * @author ZhangYuWei
 */
public interface ErpEmployeeMapper {

	//入职统计=员工表+过滤时间
	List<Map<String, Object>> findAllEntry(@Param(value="startDate")String startDate, @Param(value="endDate")String endDate);

	//离职统计=离职表+过滤时间
	List<Map<String, Object>> findAllDimission(@Param(value="startDate")String startDate, @Param(value="endDate")String endDate);

	//查询全部员工信息
	public List<Map<String,Object>> selectAllEmployee(Map<String,Object> paramsMap);

	//查询未提交离职的员工信息
	public List<Map<String,Object>> selectNotDimissionEmployee(Map<String,Object> paramsMap);

	//根据员工ID查询员工信息
	public Map<String,Object> selectByEmployeeId(Map<String,Object> paramsMap);
	
	//add by ZhangYuWei 通过员工姓名和身份证号查询员工ID
	Integer findEmpIdByIdCardNumAndName(Map<String,Object> paramsMap);
	
	Integer findEmpIdByIdCardNum(String idCardNumber);
	
	//add by ZhangYuWei 通过指定参数条件查询该项目组所有员工的信息
	List<Map<String,Object>> findEmployeeTable(Map<String,Object> params);
	
	//add by ZhangYuWei 通过员工Id查询员工信息
	Map<String,Object> findEmployeeDetail(Integer employeeId) throws Exception;
	
	//add by ZhangYuWei 查询全部员工信息
	List<Map<String,Object>> findEmployeeAll();
	
	//根据offerID查询员工信息
	public Integer selectByOfferId(Integer offerId);
	
	//插入一条员工信息
	public void insertEmployee(ErpEmployee employee);
	
	//更新一条员工信息
	public void updateEmployee(ErpEmployee employee);
	
	//删除一条员工信息
	public void deleteByEmployeeId(Integer employeeId);
	
	//通过offerId和resumeId查询出员工ID
	public Integer findEmployeeIdByOfferIdAndResumeId(ErpEmployee employee);
	
	//通过入职年月查询员工总数
	public Integer findCountByEntryTime(Map<String, Object> params);
	
	//通过（上岗或转正）工资单（或薪酬调整）状态和当前登录的用户名查询员工列表
	public List<Map<String,Object>> findEmployeeListByParams(Map<String,Object> params);
	
	// 
	public Integer findUserIdByDepartID(Integer departmentId);
	
	//add by zhangqian 离职更新员工表
	public void updateDimissionEmployeeInfo(Integer employeeId);
	
	//根据员工编号字符串查询这些员工
	public List<Map<String,Object>> findEmployeeByEmpIdArray(String employeeId);
	
	//根据员工编号查询员工的一级部门经理信息
	public Map<String,Object> findDepartmentManagerByDepId(Integer departmentId);
	
	//查询所有一级部门经理
	public List<Map<String,Object>> findDepartmentManager();
	
	//根据项目编号查询对应的员工信息
	public List<Map<String,Object>> findEmployeeByProjectId(Integer projectId);
	
	//add by 宋修功 查询HR employee 和 employee_postive【去重】关联的数据
	public List<Map<String,Object>> findEmployeeUnionMaxPositiveID();
	
	//================================教育经历================================================
	//查看员工的全部教育经历
	public List<Map<String, Object>> findAllEducationByEmp(Integer employeeId);
	//根据主键查询一条教育经历详情
	public Map<String, Object> findEducationById(Integer id);
	//添加员工的教育经历
	public void addEducationByEmp(ErpEducationExperience educationExperience);
	//修改员工的教育经历
	public void updateEducationByEmp(ErpEducationExperience educationExperience);
	//删除员工教育经历
	public void deleteEducationByEmp(Integer id);
	//通过员工ID,教育开始时间和结束时间查找 教育经历
	public Integer findEduByEmpIdStartEnd(@Param(value = "employeeId") Integer employeeId,@Param(value = "startTime") String startTime,@Param(value = "endTime") String endTime);
	//=================================工作经历======================================================
	//查看员工的工作经历
	public List<Map<String, Object>> findAllWorkExperienceByEmp(Integer employeeId);
	//添加员工的教育经历
	public void addWorkExperienceByEmp(ErpWorkExperience erpWorkExperience);
	//修改员工的教育经历
	public void updateWorkExperienceByEmp(ErpWorkExperience erpWorkExperience);
	//删除员工教育经历
	public void deleteWorkExperienceByEmp(Integer id);
	//通过员工ID,教育开始时间和结束时间查找工作经历
	public Integer findWorkByEmpIdStartEnd(@Param(value = "employeeId") Integer employeeId,@Param(value = "startTime") String startTime,@Param(value = "endTime") String endTime);
	
	//=================================项目经历======================================================
	//查看员工的工作经历
	public List<Map<String, Object>> findAllProjectExperienceByEmp(Integer employeeId);
	//添加员工的教育经历
	public void addProjectExperienceByEmp(ErpProjectExperience erpProjectExperience);
	//修改员工的教育经历
	public void updateProjectExperienceByEmp(ErpProjectExperience erpProjectExperience);
	//删除员工教育经历
	public void deleteProjectExperienceByEmp(Integer id);
	//通过员工ID,教育开始时间和结束时间查找项目经历
	public Integer findProjExperByEmpIdStartEnd(@Param(value = "employeeId") Integer employeeId,@Param(value = "startTime") String startTime,@Param(value = "endTime") String endTime);
	
	//=================================技术特长======================================================
	//查看员工的技术特长
	public List<Map<String, Object>> findAllTechnicaExpertiseByEmp(Integer employeeId);
	//添加员工的技术特长
	public void addTechnicaExpertiseByEmp(ErpTechnicaExpertise erpTechnicaExpertise);
	//修改员工的技术特长
	public void updateTechnicaExpertiseByEmp(ErpTechnicaExpertise erpTechnicaExpertise);
	//删除员工技术特长
	public void deleteTechnicaExpertiseByEmp(Integer id);
	//通过员工ID和技能名称查找重复
	public Integer findTechnicaExperByEmpIdTechnicalName(@Param(value = "employeeId") Integer employeeId,@Param(value = "technicalName") String technicalName);
	
	//=================================证书======================================================
	//查看员工证书
	public List<Map<String, Object>> findAllCertificateByEmp(Integer employeeId);
	//添加员工证书
	public void addCertificateByEmp(ErpCertificate erpCertificate);
	//修改员工证书
	public void updateCertificateByEmp(ErpCertificate erpCertificate);
	//删除员工证书
	public void deleteCertificateByEmp(Integer id);
	//根据证书主键id查看证书信息
	public Map<String, Object> seceltCertificateById(Integer id);
	//根据员工id导出员工信息
	public Map<String, Object> selectAllEmployeeById(Integer integer);
	
	//更新一条员工信息通过Map方式 add by lx
	public void updateEmployeeByMap(Map<String, Object> employee);
	
	//验证身份证号 唯一
	public Integer volidatePersonIdCard(String idCardNumber);
	//根据一级部门Id查找部门经理
	public Map<String, Object> selectManagerByFirstDepartment(Integer firstDepartment);	

	//根据员工Id 只查找单个员工表的信息
	public Map<String, Object> selectByEmployeeIdForlx(Integer employeeId);
	
	//员工信息-根据转正月份查询
	public List<Map<String,Object>> findEmployeeByPositiveMonth(String probationEndTime);

	Map<String, Object> findPostApplicantNameByEmpId(Integer employeeId);
	//通过offerId和resumeId查询是否存在相同员工
	public List<Map<String,Object>> findSameEmployeeByOfferId(Map<String,Object> map);
	//add by lx  根据一级部门id导出员工信息
	public List<Map<String,Object>> selectAllEmployeeByDepId(List<Integer>  depIds);

	//通过员工ID 证书名称 发证时间 查找重复
   public Integer findRepeatCertificateByEmpIdCertifName(@Param(value = "employeeId") Integer employeeId,@Param(value = "certificateName") String certificateName,@Param(value = "time")  String time);

	//根据id查找employee信息
	public Map<String, Object> selectEmployeeById(Integer userid);

	//根据二级部门id查找员工信息
	public List<Map<String,Object>> selectEmployeeBySId(Integer sDepartmentId);
	
	//模糊查询 5  张表
	public List<Integer> fuzzyQueryCertificate(String str);
	
	public List<Integer> fuzzyQueryEducationExperience(String str);
	
	public List<Integer> fuzzyQueryProjectExperience(String str);
	
	public List<Integer> fuzzyQueryTechnicaExpertise(String str);
	
	public List<Integer> fuzzyQueryWorkExperience(String str);
	
	//添加常用联系人
	public void addFrequentContacts(Map<String,Object> map);
	
	public void deleteFrequentContacts(Integer id);
	
	public void updateFrequentContacts(Map<String,Object> map);
	
	public List<Map<String,Object>> findFrequentContacts();

	public void addExpenseReimbursement(ExpenseReimbursementVo expenseReimbursementVo);
	
	public void addAttachmentPath(Map<String,Object> map);
	
	public List<Map<String,Object>> findExpenseReimbursement();
	
	public List<String> findAttachmentPath(Integer expReimId);
	
	public void deleteAttachmentByExpReimId(Integer id);
	
	public void deleteExpenseReimbursementById(Integer id);
	
	public String findExpenseReimbursementById(Integer id);
	
	public void deleteExpenseReimbursementById2(Integer id,String path);
	
	public void updateExpenseReimbursementByMap(Map<String,Object> map);

	//项目组管理
	public String findSimpleEmployeeById(Integer id);
	
	public Integer findPeopleCountByGroupId(Integer groupId);
	
	public List<Map<String,Object>> findPositionPeopByGroupId(Integer id);
	//项目组调动
	public void updataEmpProjectid(ErpEmployee erpuser);
	
	
	/**
	 * 获取员工名
	 * @param name
	 * @return
	 * @author hehui
	 * @createtime 2019-4-20
	 */
	public List<Map<String,Object>> getUserNameList(String name);
	
	/**
	 * 根据职位获取员工列表
	 * @param position
	 * @return
	 * @author hehui
	 * @createtime 2019-4-20
	 */
	public List<Map<String,Object>> getUserListByPosition(@Param("position") String position);

	/**
	 * 根据入职离职时间与部门查询人员列表
	 * @param queryMap
	 * @return
	 */
    List<Map<String, Object>> findEmployeeByDepartmentIdAndMonth(Map<String, Object> queryMap);
    
	/**
	 * 根据部门ID、用户名、superLeader、leader等参数查询
	 * @param queryMap
	 * @return
	 */
    List<EmployeeQueryByDeptUserVo> findEmployeeByDeptAndUser(Map<String,Object> queryMap);

	List<Map<String, Object>> findEmployeeAllByParams(Map<String, Object> param);

	/**
	 * 查询各个一级部门员工数量
	 * @return
	 */
    List<Map<String, Object>> findFirstDepartmentCountMap();

	/**
	 * 根据员工ID获取员工信息
	 * @param employeeId
	 * @return
	 */
	ErpEmployee findEmployeeDetailById(Integer employeeId) throws Exception;
	//查询项目主管管理的员工id
	public List<Integer> findProjectDirectorOfEmployee(Integer employeeId);
}
