package com.nantian.erp.hr.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpCertificate;
import com.nantian.erp.hr.data.model.ErpEducationExperience;
import com.nantian.erp.hr.data.model.ErpEmployee;
import com.nantian.erp.hr.data.model.ErpProjectExperience;
import com.nantian.erp.hr.data.model.ErpTechnicaExpertise;
import com.nantian.erp.hr.data.model.ErpWorkExperience;
import com.nantian.erp.hr.data.vo.EmployeeVo;
import com.nantian.erp.hr.data.vo.SalaryParamsVo;
import com.nantian.erp.hr.service.ErpEmployeeService;
import com.nantian.erp.hr.data.model.ExpenseReimbursement;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("erp/employee")
@Api(value = "员工相关")
public class ErpEmployeeController {

	@Autowired
	private ErpEmployeeService erpEmployeeService;

	@RequestMapping(value = "updateEmployee", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-修改员工", notes = "修改员工")
	public RestResponse updateEmployee(@RequestHeader String token, @RequestBody Map<String,Object> employee) {
		String result = erpEmployeeService.updateEmployee(token, employee);
		return RestUtils.returnSuccessWithString(result);
	}
	
	@PostMapping(value = "insertOrUpdateEmployeeFile")
	@ApiOperation(value = "新增或修改员工文件（身份证正反面、个人照片）", notes = "新增或修改员工文件（身份证正反面、个人照片）")
	public RestResponse insertOrUpdateEmployeeFile(@RequestParam MultipartFile file,@RequestParam Map<String,Object> params) {
		return erpEmployeeService.insertOrUpdateEmployeeFile(file,params);
	}
	
	@RequestMapping(value = "findAllEmployee", method = RequestMethod.GET)
	@ApiOperation(value = "员工信息-查看全部员工", notes = "查看全部员工")
	public RestResponse findAllEmployee(HttpServletRequest request) {
		String	token=	request.getHeader("token");
		String	allFlag = request.getHeader("allFlag");
		String	isDimissionPage = request.getHeader("isDimissionPage");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccess("未携带token");
		}
		
		List<EmployeeVo> result = erpEmployeeService.findAllEmployee(token, allFlag, isDimissionPage);
		if (result == null){
			return RestUtils.returnFailure("数据获取失败");
		}
		return RestUtils.returnSuccess(result);
	}
	
	@RequestMapping(value = "findEmployeeById", method = RequestMethod.GET)
	@ApiOperation(value = "员工信息-通过员工ID查看一个员工", notes = "参数是：员工ID")
	public RestResponse findEmployeeById(@RequestParam Integer employeeId) {
		return  erpEmployeeService.findEmployeeById(employeeId);
	}
	
	@RequestMapping(value = "getEmployeeRecordById", method = RequestMethod.GET)
	@ApiOperation(value = "员工信息-通过员工ID查看一个员工", notes = "参数是：员工ID")
	public RestResponse getEmployeeRecordById(@RequestParam Integer employeeId) {
		return  erpEmployeeService.getEmployeeRecordById(employeeId);
	}
	
	@RequestMapping(value = "postOrPositivePayroll", method = RequestMethod.POST)
	@ApiOperation(value = "查询待我处理（或处理完毕）上岗（或转正）工资单（或薪酬调整）的员工列表（供薪酬工程调用）",
		notes = "username用户名（可以为空）；payrollType工资单类型（post表示“上岗工资单”，positive表示“转正工资单”，adjust表示“薪酬调整”）")
	public RestResponse postOrPositivePayroll(@RequestBody SalaryParamsVo salaryParamsVo) {
		RestResponse result = erpEmployeeService.postOrPositivePayroll(salaryParamsVo);
		return result;
	}

	@RequestMapping(value = "processedPayroll", method = RequestMethod.POST)
	@ApiOperation(value = "上岗（或转正）工资单（或薪酬调整）已经处理完毕（供薪酬工程调用）",
		notes = "employeeId（员工Id）；payrollType工资单类型（post表示“上岗工资单”，positive表示“转正工资单”，adjust表示“薪酬调整”）")
	public RestResponse processedPayroll(@RequestBody SalaryParamsVo salaryParamsVo) {
		String result = erpEmployeeService.processedPayroll(salaryParamsVo);
		return RestUtils.returnSuccessWithString(result);
	}
	
	@RequestMapping(value = "adjustSalary", method = RequestMethod.POST)
	@ApiOperation(value = "查询待我处理（或处理完毕）薪酬调整的员工列表（供薪酬工程调用）",notes = "username用户名（可以为空）")
	public RestResponse adjustSalary(@RequestBody SalaryParamsVo salaryParamsVo) {
		RestResponse result = erpEmployeeService.adjustSalary(salaryParamsVo);
		return result;
	}
	
	@RequestMapping(value = "findEmployeeByEmpIdArray", method = RequestMethod.GET)
	@ApiOperation(value = "员工信息-通过员工ID查询这些员工信息", notes = "参数是：员工ID的字符串")
	public RestResponse findEmployeeByEmpIdArray(@RequestParam String employeeId) {
		return  erpEmployeeService.findEmployeeByEmpIdArray(employeeId);
	}
	
	@RequestMapping(value = "findDepMaByEmpId", method = RequestMethod.GET)
	@ApiOperation(value = "根据员工编号查询员工的部门经理信息", notes = "参数是：员工ID")
	public RestResponse findDepMaByEmpId(@RequestParam Integer employeeId) {
		return  erpEmployeeService.findDepartmentManagerByEmpId(employeeId);
	}
	
	@RequestMapping(value = "findEmployeeByEmpIdArrayP", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-通过员工ID查询这些员工信息", notes = "参数是：员工ID的字符串")
	public RestResponse findEmployeeByEmpIdArrayP(@RequestBody Map<String,Object> map) {
		return  erpEmployeeService.findEmployeeByEmpIdArray((String)(((List)map.get("employeeId")).get(0)));
	}
	
	@RequestMapping(value = "findDepartmentManager", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有一级部门经理信息", notes = "没有参数")
	public RestResponse findDepartmentManager() {
		return  erpEmployeeService.findDepartmentManager();
	}
	
	// ==================================== add by ZhangYuWei 供其他工程调用（开始） ====================================
	@RequestMapping(value = "/findEmpIdByIdCardNumAndName", method = RequestMethod.POST)
	@ApiOperation(value = "通过员工姓名和身份证号查询员工ID（供薪酬工程调用）", notes = "参数是：姓名、身份证号")
	public RestResponse findEmpIdByIdCardNumAndName(@RequestParam Map<String,Object> requestParams) {
		RestResponse employeeId = erpEmployeeService.findEmpIdByIdCardNumAndName(requestParams);
		return employeeId;
	}
	
	@RequestMapping(value = "/findSecDepEmpInfoByFirDepId", method = RequestMethod.GET)
	@ApiOperation(value = "通过一级部门ID查询所有分组的二级部门的员工信息（供薪酬工程调用）", notes = "参数是：一级部门ID；返回结果是二维数组")
	public RestResponse findSecDepEmpInfoByFirDepId(@RequestParam Integer firstDepartmentId) {
		RestResponse employeeList = erpEmployeeService.findSecDepEmpInfoByFirDepId(firstDepartmentId);
		return employeeList;
	}
	
	@RequestMapping(value = "/findEmpInfoOfAllFirDepByParams", method = RequestMethod.POST)
	@ApiOperation(value = "条件查询所有一级部门下的员工信息（供薪酬工程调用）", notes = "参数是：一级部门经理用户编号、部门类别、上级领导；返回结果是三维数组")
	public RestResponse findEmpInfoOfAllFirDepByParams(@RequestBody Map<String,Object> params) {
		RestResponse employeeList = erpEmployeeService.findEmpInfoOfAllFirDepByParams(params);
		return employeeList;
	}
	
	@RequestMapping(value = "/findEmployeeDetail", method = RequestMethod.GET)
	@ApiOperation(value = "通过员工Id查询员工信息（供薪酬工程调用）", notes = "参数是：员工Id")
	public RestResponse findEmployeeDetail(@RequestParam Integer employeeId) {
		RestResponse employeeList = erpEmployeeService.findEmployeeDetail(employeeId);
		return employeeList;
	}
	
	@RequestMapping(value = "/findEmployeeAll", method = RequestMethod.GET)
	@ApiOperation(value = "查询全部的员工信息（供薪酬工程调用）", notes = "参数是：无")
	public RestResponse findEmployeeAll() {
		RestResponse employeeList = erpEmployeeService.findEmployeeAll();
		return employeeList;
	}
	
	@PostMapping(value = "/findEmployeeTable")
	@ApiOperation(value = "查找员工表信息（供项目工程调用）", notes = "参数是：项目组ID等（参数可以为空）；返回结果是一维数组")
	public RestResponse findEmployeeTable(@RequestBody(required=false) Map<String,Object> params) {
		return erpEmployeeService.findEmployeeTable(params);
	}
	
	/*
	 * 以下接口需要增加到过滤器中的白名单中，不需要验证token，不用配置URL权限
	 * 定时器调用的接口、登录时调用的接口
	 */
	@RequestMapping(value = "/findEmployeeAllForScheduler", method = RequestMethod.GET)
	@ApiOperation(value = "查询全部的员工信息（供薪酬工程调用）", notes = "参数是：无")
	public RestResponse findEmployeeAllForScheduler() {
		RestResponse employeeList = erpEmployeeService.findEmployeeAll();
		return employeeList;
	}
	
	@RequestMapping(value = "/findEmpInfoOfAllFirDepByParamsScheduler", method = RequestMethod.POST)
	@ApiOperation(value = "条件查询所有一级部门下的员工信息（供薪酬工程调用）", notes = "参数是：一级部门经理用户编号、部门类别、上级领导；返回结果是三维数组")
	public RestResponse findEmpInfoOfAllFirDepByParamsScheduler(@RequestBody Map<String,Object> params) {
		RestResponse employeeList = erpEmployeeService.findEmpInfoOfAllFirDepByParams(params);
		return employeeList;
	}
	
	@RequestMapping(value = "/findEmployeeDetailForLogin", method = RequestMethod.GET)
	@ApiOperation(value = "通过员工Id查询员工信息（供权限工程调用）", notes = "参数是：员工Id")
	public RestResponse findEmployeeDetailForLogin(@RequestParam Integer employeeId) {
		RestResponse employeeList = erpEmployeeService.findEmployeeDetail(employeeId);
		return employeeList;
	}
	
	@PostMapping(value = "/findEmployeeTableForScheduler")
	@ApiOperation(value = "查找员工表信息（供项目工程调用）", notes = "参数是：项目组ID等（参数可以为空）；返回结果是一维数组")
	public RestResponse findEmployeeTableForScheduler(@RequestBody(required=false) Map<String,Object> params) {
		return erpEmployeeService.findEmployeeTable(params);
	}
	
	@RequestMapping(value = "/findEmployeeByDeptAndUser", method = RequestMethod.POST)
	@ApiOperation(value = "根据员工部门ID和员工名称查询一级部门下管辖的二级部门的员工信息",notes = "参数：deptId，employeeName，page，offset")
	public RestResponse findEmployeeByDeptAndUser(@RequestHeader String token, @RequestBody Map<String,Object> params) {
		RestResponse employeeList = erpEmployeeService.findEmployeeByDeptAndUser(token,params);
		return employeeList;
	}

	@RequestMapping(value = "/findEmployeeByDeptAndUserNoToken", method = RequestMethod.POST)
	@ApiOperation(value = "根据员工部门ID和员工名称查询一级部门下管辖的二级部门的员工信息",notes = "参数：deptId，employeeName，page，offset")
	public RestResponse findEmployeeByDeptAndUserNoToken(@RequestBody Map<String,Object> params) {
		RestResponse employeeList = erpEmployeeService.findEmployeeByDeptAndUserNoToken(params);
		return employeeList;
	}
	// ==================================== add by ZhangYuWei 供其他工程调用（结束） ====================================

	
	// ==================================== add by ZhangYuWei 教育经历（开始）====================================
	@GetMapping(value = "findAllEducationByEmp")
	@ApiOperation(value = "员工信息-通过员工id获取员工的教育经历", notes = "查看教育经历")
	public RestResponse findAllEducationByEmp(@RequestParam Integer employeeId) {
		return erpEmployeeService.findAllEducationByEmp(employeeId);
	}
	
	@PostMapping(value = "addEducationByEmp")
	@ApiOperation(value = "员工信息-添加教育经历", notes = "添加教育经历")
	public RestResponse addEducationByEmp(MultipartFile file,MultipartFile file1,ErpEducationExperience educationExperience) {
		return erpEmployeeService.addEducationByEmp(file,file1,educationExperience);
	}

	@GetMapping(value = "deleteEducationByEmp")
	@ApiOperation(value = "员工信息-删除教育经历", notes = "删除教育经历")
	public RestResponse deleteEducationByEmp(@RequestParam Integer id) {
		return erpEmployeeService.deleteEducationByEmp(id);
	}
	
	@PostMapping(value = "updateEducationByEmp")
	@ApiOperation(value = "员工信息-修改教育经历", notes = "修改教育经历")
	public RestResponse updateEducationByEmp(MultipartFile file,MultipartFile file1,ErpEducationExperience educationExperience) {
		return erpEmployeeService.updateEducationByEmp(file,file1,educationExperience);
	}
	
	@GetMapping(value = "downloadEducationByEmp")
	@ApiOperation(value = "教育经历下载（因为图片有多个，所以参数是文件名，不是主键）", notes = "证书下载")
	public RestResponse downloadEducationByEmp(@RequestParam String fileName) {
		return erpEmployeeService.downloadEducationByEmp(fileName);
	}
	// ==================================== add by ZhangYuWei 教育经历（结束）====================================

	
	// ========================================工作经历===========================================================
	
	@RequestMapping(value = "findAllWorkExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-通过员工id获取员工的工作经历", notes = "查看工作经历")
	public RestResponse findAllWorkExperienceByEmp(@RequestBody ErpEmployee erpEmployee) {
		Integer employeeId = erpEmployee.getEmployeeId();
		List<Map<String, Object>> resultList = erpEmployeeService.findAllWorkExperienceByEmp(employeeId);
		return RestUtils.returnSuccess(resultList);
	}
	
	@RequestMapping(value = "addWorkExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-添加工作经历", notes = "添加工作经历")
	public RestResponse addWorkExperienceByEmp(@RequestBody ErpWorkExperience erpWorkExperience) {
		String resultStr = erpEmployeeService.addWorkExperienceByEmp(erpWorkExperience);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value = "deleteWorkExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-删除工作经历", notes = "删除工作经历")
	public RestResponse deleteWorkExperienceByEmp(@RequestBody ErpWorkExperience erpWorkExperience) {
		Integer id = erpWorkExperience.getId();
		String resultStr = erpEmployeeService.deleteWorkExperienceByEmp(id);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value = "updateWorkExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-修改工作经历", notes = "修改工作经历")
	public RestResponse updateWorkExperienceByEmp(@RequestBody ErpWorkExperience erpWorkExperience) {
		String resultStr = erpEmployeeService.updateWorkExperienceByEmp(erpWorkExperience);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	// ==================================项目经历=========================================
	
	@RequestMapping(value = "findAllProjectExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-通过员工id获取员工的项目经历", notes = "查看项目经历")
	public RestResponse findAllProjectExperienceByEmp(@RequestBody ErpEmployee erpEmployee) {
		Integer employeeId = erpEmployee.getEmployeeId();
		List<Map<String, Object>> resultList = erpEmployeeService.findAllProjectExperienceByEmp(employeeId);
		return RestUtils.returnSuccess(resultList);
	}

	
	@RequestMapping(value = "addProjectExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-添加项目经历", notes = "添加项目经历")
	public RestResponse addProjectExperienceByEmp(@RequestBody ErpProjectExperience erpProjectExperience) {
		String resultStr = erpEmployeeService.addProjectExperienceByEmp(erpProjectExperience);
		return RestUtils.returnSuccessWithString(resultStr);
	}

	
	@RequestMapping(value = "updateProjectExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-修改项目经历", notes = "修改项目经历")
	public RestResponse updateProjectExperienceByEmp(@RequestBody ErpProjectExperience erpProjectExperience) {
		String resultStr = erpEmployeeService.updateProjectExperienceByEmp(erpProjectExperience);
		return RestUtils.returnSuccessWithString(resultStr);
	}

	
	@RequestMapping(value = "deleteProjectExperienceByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-删除项目经历", notes = "删除项目经历")
	public RestResponse deleteProjectExperienceByEmp(@RequestBody ErpProjectExperience erpProjectExperience) {
		Integer id = erpProjectExperience.getId();
		String resultStr = erpEmployeeService.deleteProjectExperienceByEmp(id);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	// ==================================技术特长=========================================
	
		@RequestMapping(value = "findAllTechnicaExpertiseByEmp", method = RequestMethod.POST)
		@ApiOperation(value = "员工信息-通过员工id获取员工的技术特长", notes = "查看技术特长")
		public RestResponse findAllTechnicaExpertiseByEmp(@RequestBody ErpEmployee erpEmployee) {
			Integer employeeId = erpEmployee.getEmployeeId();
			List<Map<String, Object>> resultList = erpEmployeeService.findAllTechnicaExpertiseByEmp(employeeId);
			return RestUtils.returnSuccess(resultList);
		}

		
		@RequestMapping(value = "addTechnicaExpertiseByEmp", method = RequestMethod.POST)
		@ApiOperation(value = "员工信息-添加技术特长", notes = "添加项技术特长")
		public RestResponse addTechnicaExpertiseByEmp(@RequestBody ErpTechnicaExpertise erpTechnicaExpertise) {
			String resultStr = erpEmployeeService.addTechnicaExpertiseByEmp(erpTechnicaExpertise);
			return RestUtils.returnSuccessWithString(resultStr);
		}

		
		@RequestMapping(value = "updateTechnicaExpertiseByEmp", method = RequestMethod.POST)
		@ApiOperation(value = "员工信息-修改技术特长", notes = "修改技术特长")
		public RestResponse updateTechnicaExpertiseByEmp(@RequestBody ErpTechnicaExpertise erpTechnicaExpertise) {
			String resultStr = erpEmployeeService.updateTechnicaExpertiseByEmp(erpTechnicaExpertise);
			return RestUtils.returnSuccessWithString(resultStr);
		}

		
		@RequestMapping(value = "deleteTechnicaExpertiseByEmp", method = RequestMethod.POST)
		@ApiOperation(value = "员工信息-删除技术特长", notes = "删除技术特长")
		public RestResponse deleteTechnicaExpertiseByEmp(@RequestBody ErpTechnicaExpertise erpTechnicaExpertise) {
			Integer id = erpTechnicaExpertise.getId();
			String resultStr = erpEmployeeService.deleteTechnicaExpertiseByEmp(id);
			return RestUtils.returnSuccessWithString(resultStr);
		}

	// ==================================证书=========================================
	
	@RequestMapping(value = "findAllCertificateByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-通过员工id获取员工的证书", notes = "查看员工证书")
	public RestResponse findAllCertificateByEmp(@RequestBody ErpEmployee erpEmployee) {
		Integer employeeId = erpEmployee.getEmployeeId();
		List<Map<String, Object>> resultList = erpEmployeeService.findAllCertificateByEmp(employeeId);
		return RestUtils.returnSuccess(resultList);
	}

	
	@RequestMapping(value = "addCertificateByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-添加员工证书", notes = "添加员工证书")
	public RestResponse addCertificateByEmp(MultipartFile file,ErpCertificate erpCertificate) throws IOException{
		RestResponse result = erpEmployeeService.addCertificateByEmp(file,erpCertificate);
		return result;
	}

	
	@RequestMapping(value = "updateCertificateByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-修改员工证书", notes = "修改员工证书")
	public RestResponse updateCertificateByEmp(MultipartFile file,ErpCertificate erpCertificate) throws IOException {
		RestResponse resultStr = erpEmployeeService.updateCertificateByEmp(file,erpCertificate);
		return resultStr;
	}

	
	@RequestMapping(value = "deleteCertificateByEmp", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-删除员工证书", notes = "删除员工证书")
	public RestResponse deleteCertificateByEmp(@RequestBody ErpCertificate erpCertificate) {
		Integer id = erpCertificate.getId();
		String resultStr = erpEmployeeService.deleteCertificateByEmp(id);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	
	@RequestMapping(value = "downloadCertificate", method = RequestMethod.GET)
	@ApiOperation(value = "证书下载", notes = "证书下载")
	public RestResponse downloadCertificate(@RequestParam Integer id, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		RestResponse result = erpEmployeeService.downloadCertificate(id, request, response);
		return result;
	}
	
	
	
	@RequestMapping(value = "findEmployeeByPositiveMonth", method = RequestMethod.GET)
	@ApiOperation(value = "员工信息-根据转正月份查询", notes = "员工信息-根据转正月份查询")
	public RestResponse findEmployeeByPositiveMonth(@RequestParam String probationEndTime) {
		List<Map<String, Object>> resultList=erpEmployeeService.findEmployeeByPositiveMonth(probationEndTime);
		return RestUtils.returnSuccess(resultList);
	}
	
	@RequestMapping(value = "findEmpInfoById", method = RequestMethod.POST)
	@ApiOperation(value = "员工信息-通过员工ID查看员工信息", notes = "参数是：员工ID列表")
	public RestResponse findEmpInfoById(@RequestHeader String token, @RequestBody List<Map<String,Object>> employeeIdList) {
		return  erpEmployeeService.findEmpInfoById(token, employeeIdList);
	}
	
	@RequestMapping(value = "findPostApplicantNameByEmpId", method = RequestMethod.GET)
	@ApiOperation(value = "员工信息-通过员工ID查看其岗位申请人姓名", notes = "参数是：员工ID")
	public RestResponse findPostApplicantNameByEmpId(@RequestParam Integer employeeId) {
		return  erpEmployeeService.findPostApplicantNameByEmpId(employeeId);
	}
	
	// ==================================薪酬调整模块=========================================
	
	/*	@RequestMapping(value = "findEmpInfoById", method = RequestMethod.GET)
		@ApiOperation(value = "员工信息-通过员工ID查看一个员工", notes = "参数是：员工ID")
		public RestResponse findEmpInfoById(@RequestParam Integer employeeId) {
			return  erpEmployeeService.findSEmpInfoById(employeeId);
		}*/
		
		@RequestMapping(value = "findEmpNameMapById", method = RequestMethod.POST)
		@ApiOperation(value = "员工信息-通过员工ID查看一个员工", notes = "参数是：map")
		public RestResponse findEmpNameMapById(@RequestBody Map<String,	Object> map) {
			return  erpEmployeeService.findEmpNameMapById(map);
		}
		
		@RequestMapping(value = "findEmpNameByEIdMap", method = RequestMethod.POST)
		@ApiOperation(value = "员工信息-通过员工map得到ID查看员工名称", notes = "参数是：map")
		public RestResponse findEmpNameByEIdMap(@RequestBody Map<String,Object> map) {
			return  erpEmployeeService.findEmpNameByEIdMap(map);
		}
		
		
//	@RequestMapping(value = "/exportResumeByEmployeeId", method = RequestMethod.GET)
//	@ApiOperation(value = "根据员工Id导出员工简历", notes = "参数是：员工ID")
//	public RestResponse exportResumeByEmployeeId(@RequestParam Integer employeeId) {
//		return  erpEmployeeService.exportResumeByEmployeeId(employeeId);
//	}
	
	@RequestMapping(value = "/exportResumeMoreByEmployeeIds", method = RequestMethod.POST)
	@ApiOperation(value = "根据员工Id导出员工简历（支持批量）", notes = "参数是：员工ID（数组）")
	public RestResponse exportResumeMoreByEmployeeIds(@RequestBody List<Integer> employeeIds) {
		return erpEmployeeService.exportResumeMoreByEmployeeIds(employeeIds);
	}
	
	@RequestMapping(value = "/previewResumeByEmployeeId", method = RequestMethod.GET)
	@ApiOperation(value = "根据员工Id预览员工简历", notes = "参数是：员工ID")
	public RestResponse previewResumeByEmployeeId(@RequestParam Integer employeeId) {
		return erpEmployeeService.previewResumeByEmployeeId(employeeId);
	}
	
	//@Scheduled(cron = "0 21 * * * ?")
	@Scheduled(cron = "0 30 1 * * ?")
	public void automaticDeleteTempFilesScheduler() {
		erpEmployeeService.automaticDeleteTempFilesScheduler();
	}
	
//	@Scheduled(cron = "0 30 1 * * ?")
	@RequestMapping(value = "automaticComareEmpSalary", method = RequestMethod.GET)
	@ApiOperation(value = "HR和salary数据一致性检查，由Salary调用", notes = "参数是：无")
	public RestResponse automaticComareEmpSalary() {
		//HR和salary数据一致性检查，由Salary调用
		return erpEmployeeService.automaticComareEmpSalary();
	}
	
	// ===================================行政信息=========================================
	@RequestMapping(value = "addFrequentContacts", method = RequestMethod.POST)
	@ApiOperation(value = "添加常用联系人", notes = "参数是：map")
	public RestResponse addFrequentContacts(@RequestBody Map<String,Object> map) {
		return erpEmployeeService.addFrequentContacts(map);
	}
	
	@PostMapping(value = "updateFrequentContacts")
	@ApiOperation(value = "更新常用联系人", notes = "参数是：map")
	public RestResponse updateFrequentContacts(@RequestBody Map<String,Object> map) {
		return erpEmployeeService.updateFrequentContacts(map);
	}
	@GetMapping(value = "deleteFrequentContacts")
	@ApiOperation(value = "删除常用联系人", notes = "参数是：integer")
	public RestResponse deleteFrequentContacts(@RequestParam Integer id) {
		return erpEmployeeService.deleteFrequentContacts(id);
	}
	@PostMapping(value = "findFrequentContacts")
	@ApiOperation(value = "查找常用联系人", notes = "参数是：")
	public RestResponse findFrequentContacts() {
		return erpEmployeeService.findFrequentContacts();
	}
	
	@PostMapping(value = "addExpenseReimbursement")
	@ApiOperation(value = "添加费用报销", notes = "参数是：文件  ")
	public RestResponse addExpenseReimbursement(@RequestParam("PhotoFile") MultipartFile  PhotoFile,@RequestParam("attachment") MultipartFile[]  attachment,@RequestParam String title,@RequestParam String notes) {	
		return erpEmployeeService.addExpenseReimbursement(PhotoFile,attachment,title, notes);
	}
	
	@PostMapping(value = "findExpenseReimbursement")
	@ApiOperation(value = "查找费用报销", notes = "参数是：无  ")
	public RestResponse findExpenseReimbursement() {
		return erpEmployeeService.findExpenseReimbursement();
	}
	
	@PostMapping(value = "deleteExpenseReimbursementByMap")
	@ApiOperation(value = "删除费用报销", notes = "参数是：map  ")
	public RestResponse deleteExpenseReimbursementByMap(@RequestBody Map<String,Object> map) {
		return erpEmployeeService.deleteExpenseReimbursementByMap(map);
	}
	
	@PostMapping(value = "updateExpenseReimbursementByMap")
	@ApiOperation(value = "修改费用报销", notes = "参数是：文件  ")
	public RestResponse updateExpenseReimbursementByMap(ExpenseReimbursement expenseReimbursement) {	
		return erpEmployeeService.updateExpenseReimbursementByMap(expenseReimbursement);
	}
	
	@RequestMapping(value = "/previewAttachment", method = RequestMethod.GET)
	@ApiOperation(value = "预览附件", notes = "预览附件")
	public RestResponse previewResume(@RequestParam String attachmentPath,@RequestHeader String token) {
		RestResponse result = erpEmployeeService.previewAttachment(attachmentPath,token);
		return result;
	}
	
	@RequestMapping(value = "/downloadAttachment", method = RequestMethod.GET)
	@ApiOperation(value = "费用报销附件下载", notes = "简历下载")
	public RestResponse downloadAttachment(@RequestParam String attachmentPath) {
		RestResponse result = erpEmployeeService.downloadAttachment(attachmentPath);
		return result;
	}
	
	//=========================== 项目组管理   =============================//
	
	@RequestMapping(value = "/findFirstDepAndEmp", method = RequestMethod.POST)
	@ApiOperation(value = "查找全部一级部门", notes = "")
	public RestResponse findFirstDepAndEmp(@RequestBody Map<String,Object> listMap) {
		return erpEmployeeService.findFirstDepAndEmp(listMap);
	}
	
	@RequestMapping(value = "/findProGroDetailsByGId", method = RequestMethod.GET)
	@ApiOperation(value = "跨工程查询-通过id查询项目组详细", notes = "id")
	public RestResponse findProGroDetailsByGId(Integer id) {
		return erpEmployeeService.findProGroDetailsByGId(id);
	}
	
	@RequestMapping(value = "/updateProjectIdForScheduler", method = RequestMethod.POST)
	@ApiOperation(value = "工作调动-修改员工关联项目组id", notes = "")
	public RestResponse updateProjectIdForScheduler(@RequestBody Map<String,Object> params){
		return erpEmployeeService.updateProjectId(params);
	}
	
	//=========================== 项目 ===================================//
	
	@RequestMapping(value = "/findProjectMessHr", method = RequestMethod.POST)
	@ApiOperation(value = "跨工程1.往项目list中添加manager名称 2.往售前项目list中添加销售责任人和售前项目责任人名称", notes = "map")
	public RestResponse findProjectMessHr(@RequestBody Map<String,Object> listMap) {
		return erpEmployeeService.findProjectMessHr(listMap);
	}
	
	@RequestMapping(value = "/findEmployeeByEmpProjectId", method = RequestMethod.GET)
	@ApiOperation(value = "查询员工所在项目的所有员工信息", notes = "员工id")
	public RestResponse findEmployeeByEmpProjectId(Integer employeeId) {
		return erpEmployeeService.findEmployeeByEmpProjectId(employeeId);
	}

	@RequestMapping(value = "/findEmployeeInfoMap", method = RequestMethod.GET)
	@ApiOperation(value = "查询员工MAP信息", notes = "部门名称、员工名称关键字")
	public RestResponse findEmployeeInfoMap(@RequestParam(required = false) String departmentName, @RequestParam(required = false) String employeeName) {
		return erpEmployeeService.findEmployeeInfoMap(departmentName, employeeName);
	}
	
	//============================ 请假 ====================================//
	@RequestMapping(value = "findSimpleEmpById", method = RequestMethod.GET)
	@ApiOperation(value = "项目工程跨工程调用查询员工信息", notes = "参数是：员工ID")
	public RestResponse findSimpleEmpById(@RequestParam Integer employeeId) {
		return  erpEmployeeService.findSimpleEmpById(employeeId);
	}
	
	/**
	 * @param name
	 * @return
	 * @author hehui
	 * @createtime 2019-4-20
	 */
	@GetMapping(value = "/getUserNameList")
	@ApiOperation(value = "用户姓名联想框(供项目工程调用)", notes = "返回的是包含id和name的map集合，参数为空时获取所有用户")
	public RestResponse getUserNameList(@RequestParam String name) {
		return this.erpEmployeeService.getUserNameList(name);
	}
	
	/**
	 * @param position
	 * @return
	 * @author hehui
	 * @createtime 2019-4-20
	 */
	@GetMapping(value = "/getUserListByPosition")
	@ApiOperation(value = "根据职位获取的员工列表(供项目工程调用)", notes = "返回的是包含id和name的map集合，参数为空时获取所有用户")
	public RestResponse getUserListByPosition(@RequestParam String position) {
		return this.erpEmployeeService.getUserListByPosition(position);
	}
	
	@RequestMapping(value = "/findAllGroups", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有民族", notes = "查询所有民族")
	public RestResponse findAllGroups() {
		List<Map<String,Object>> allGroups = erpEmployeeService.findAllGroups();
		return RestUtils.returnSuccess(allGroups);
	}
	
	@RequestMapping(value = "/findAllPolitical", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有政治面貌", notes = "查询所有政治面貌")
	public RestResponse findAllPolitical() {
		List<Map<String,Object>> allGroups = erpEmployeeService.findAllPolitical();
		return RestUtils.returnSuccess(allGroups);
	}
	
	@RequestMapping(value="/updateEmployeeBySalary",method=RequestMethod.POST)
	@ApiOperation(value="跨工程更新员工信息",notes="跨工程更新员工信息")
	public RestResponse updateEmployeeBySalary(@RequestBody ErpEmployee params) {
		return erpEmployeeService.updateEmployeeBySalary(params);
	}

	/**
	 * 修改手机号
	 * 短信验证
	 * @author xjh
	 * @since 2020年5月6日17:23:42
	 */
	@RequestMapping(value = "/sendsms", method = RequestMethod.GET)
	@ApiOperation(value = "发送验证短信", notes = "参数：用户id")
	public RestResponse sendSms(@RequestHeader String token, @RequestParam Integer employeeId, @RequestParam Integer type, @RequestParam(required = false) Integer changeType) {
		return erpEmployeeService.sendSms(token, employeeId, type, changeType);
	}

	@RequestMapping(value = "/checksms", method = RequestMethod.GET)
	@ApiOperation(value = "校验验证短信", notes = "参数：验证码")
	public RestResponse checkSms(@RequestParam String code, @RequestParam Integer employeeId,@RequestHeader String token) {
		return erpEmployeeService.checkSms(code, employeeId, token);
	}
	
	@RequestMapping(value = "/employeeInProjectInfo", method = RequestMethod.POST)
	@ApiOperation(value = "部门模块中员工在项信息", notes = "参数：")
	public RestResponse employeeInProjectInfo(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		return erpEmployeeService.employeeInProjectInfo(token,params);
	}
	@RequestMapping(value = "/projectInEmployees", method = RequestMethod.POST)
	@ApiOperation(value = "查询项目主管和项目经理管理员工信息", notes = "参数：")
	public RestResponse projectInEmployees(@RequestBody Map<String,Object> params) {
		return erpEmployeeService.projectInEmployees(params);
	}
}
