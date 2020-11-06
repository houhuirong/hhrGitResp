package com.nantian.erp.salary.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.model.ErpSocialSecurity;
import com.nantian.erp.salary.service.AutomaticPositionScheduler;
import com.nantian.erp.salary.service.ErpBasePayrollService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 薪酬管理controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年10月22日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("salary/basePayroll")
@Api(value = "薪酬管理")
public class ErpBasePayrollController {
	
	@Autowired
	private ErpBasePayrollService erpBasePayrollService;
	
	@Autowired
	private AutomaticPositionScheduler automaticPositionScheduler;
	
	@ApiOperation(value = "下载基本薪资导入模板", notes = "参数：无参数")
	@RequestMapping(value = "/downloadBasePayroll", method = RequestMethod.POST)
	public RestResponse downloadBasePayroll() {
		RestResponse result = erpBasePayrollService.downloadBasePayroll();
		return result;
	}
	
	@ApiOperation(value = "员工薪酬信息导入", notes = "参数：员工薪酬信息Excel文件")
	@RequestMapping(value = "/importBasePayroll", method = RequestMethod.POST)
	public RestResponse importBasePayroll(MultipartFile file,@RequestHeader String token) {
		RestResponse result = erpBasePayrollService.importBasePayroll(file,token);
		return result;
	}
	
	@ApiOperation(value = "根据一级部门ID导出员工薪酬信息", notes = "参数：一级部门ID")
	@RequestMapping(value = "/exportBasePayroll", method = RequestMethod.GET)
	public RestResponse exportBasePayroll(@RequestParam Integer firstDepartmentId,@RequestParam(required = false) String queryMode, @RequestHeader String token) {
		RestResponse result = erpBasePayrollService.exportBasePayroll(firstDepartmentId,queryMode,token);
		return result;
	}

	@ApiOperation(value = "导出权限内所有一级部门的员工信息和薪酬情况", notes = "参数：token、部门类别、查询模式")
	@RequestMapping(value = "/exportFirDepEmpInfoByPowerParams", method = RequestMethod.GET)
	public RestResponse exportFirDepEmpInfoByPowerParams(@RequestHeader String token,@RequestParam String departmentType,@RequestParam String queryMode,String keyword) {
		return erpBasePayrollService.exportFirDepEmpInfoByPowerParams(token,departmentType,queryMode,keyword);
	}
	
	@ApiOperation(value = "查询所有一级部门", notes = "参数:无参数")
	@RequestMapping(value = "/findAllFirstDepartment", method = RequestMethod.GET)
	public RestResponse findAllFirstDepartment(@RequestHeader String token) {
		RestResponse result = erpBasePayrollService.findAllFirstDepartment(token);
		return result;
	}
	
	@ApiOperation(value = "查询一级部门下全部二级部门的员工信息和薪酬情况", notes = "参数：一级部门ID")
	@RequestMapping(value = "/findSecDepEmpInfoByFirDepId", method = RequestMethod.GET)
	public RestResponse findSecDepEmpInfoByFirDepId(@RequestParam Integer firstDepartmentId,@RequestHeader String token) {
		RestResponse result = erpBasePayrollService.findSecDepEmpInfoByFirDepId(firstDepartmentId,token);
		return result;
	}
	
	@ApiOperation(value = "条件查询新增员工薪酬信息失败的日志记录信息", notes = "参数：页码、行数、开始时间、结束时间")
	@RequestMapping(value = "/findBasePayrollRecord", method = RequestMethod.POST)
	public RestResponse findBasePayrollRecord(@RequestBody Map<String,Object> params) {
		RestResponse result = erpBasePayrollService.findBasePayrollRecord(params);
		return result;
	}
	
	@ApiOperation(value = "查询权限内所有一级部门的员工信息和薪酬情况", notes = "参数：token、部门类别、查询模式")
	@RequestMapping(value = "/findFirDepEmpInfoByPowerParams", method = RequestMethod.GET)
	public RestResponse findFirDepEmpInfoByPowerParams(@RequestHeader String token,@RequestParam String departmentType,@RequestParam String queryMode,String keyword) {
		RestResponse result = erpBasePayrollService.findFirDepEmpInfoByPowerParams(token,departmentType,queryMode,keyword);
		return result;
	}
	
	//@Scheduled(cron = "0 30 * * * ?")
	@Scheduled(cron = "0 0 0 5 * ?")
	public void automaticCreateDepartmentCostMonthScheduler() {
		erpBasePayrollService.automaticCreateDepartmentCostMonthScheduler();
	}
	
	@RequestMapping(value = "/updateCreateDepartmentCostMonth", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "薪酬分析-刷新、更新部门人员费用统计", notes = "无参数")
	public RestResponse updateCreateDepartmentCostMonth() {
		RestResponse result = erpBasePayrollService.automaticCreateDepartmentCostMonthScheduler();
		return result;
	}
	
	@ApiOperation(value = "薪酬分析-查询所有部门的人员费用合计", notes = "参数：起止年月")
	@RequestMapping(value = "/findAllDepartmentCostMonth", method = RequestMethod.GET)
	public RestResponse findAllDepartmentCostMonth(@RequestParam String startTime,@RequestParam String endTime,@RequestHeader String token) {
		RestResponse result = erpBasePayrollService.findAllDepartmentCostMonth(startTime,endTime,token);
		return result;
	}
	
	@ApiOperation(value = "薪酬分析-查询指定部门的人员费用合计", notes = "参数：一级部门Id")
	@RequestMapping(value = "/findOneDepartmentCostMonth", method = RequestMethod.GET)
	public RestResponse findOneDepartmentCostMonth(@RequestParam Integer firstDepartmentId) {
		RestResponse result = erpBasePayrollService.findOneDepartmentCostMonth(firstDepartmentId);
		return result;
	}
	
	@ApiOperation(value = "新增社保缴纳比例、社保缴纳基数上下限", notes = "参数：社保表字段")
	@RequestMapping(value = "/insertSocialSecurity", method = RequestMethod.POST)
	public RestResponse insertSocialSecurity(@RequestBody ErpSocialSecurity socialSecurity) {
		RestResponse result = erpBasePayrollService.insertSocialSecurity(socialSecurity);
		return result;
	}
	
	@ApiOperation(value = "修改社保缴纳比例、社保缴纳基数上下限", notes = "参数：社保表主键")
	@RequestMapping(value = "/updateSocialSecurity", method = RequestMethod.POST)
	public RestResponse updateSocialSecurity(@RequestBody ErpSocialSecurity socialSecurity) {
		RestResponse result = erpBasePayrollService.updateSocialSecurity(socialSecurity);
		return result;
	}
	
	@ApiOperation(value = "查询社保缴纳比例、社保缴纳基数上下限", notes = "无参数")
	@RequestMapping(value = "/findSocialSecurity", method = RequestMethod.GET)
	public RestResponse findSocialSecurity() {
		RestResponse result = erpBasePayrollService.findSocialSecurity();
		return result;
	}
	
	@ApiOperation(value="查询员工谈薪、试用期和上岗薪资等",notes="参数:offerId、employeeId")
	@RequestMapping(value="/findEmpAllSalaryDetail",method=RequestMethod.GET)
	public RestResponse findEmpAllSalaryDetail(@RequestParam Integer offerId,@RequestParam Integer employeeId){
		RestResponse result=erpBasePayrollService.findEmpAllSalaryDetail(offerId,employeeId);
		return result;
	}
	
	@ApiOperation(value="HR Salary数据一致性校验",notes="参数:token")
	@RequestMapping(value="/automaticComareEmpSalary",method=RequestMethod.GET)
	public RestResponse automaticComareEmpSalary(@RequestHeader String token){
		return automaticPositionScheduler.automaticComareEmpSalary(token);
	}
	
	@ApiOperation(value="上岗转正薪资根据员工转正日期更新到实时薪资中",notes="参数:token")
	@RequestMapping(value="/checkAndUpdateBaseSalary",method=RequestMethod.POST)
	public RestResponse checkAndUpdateBaseSalary(@RequestHeader String token){
//		automaticPositionScheduler.automaticComareEmpSalary(token);
		return automaticPositionScheduler.checkAndUpdateBaseSalary(token);
	}
	
	@ApiOperation(value = "导出员工薪酬汇总", notes = "参数：一级部门ID")
	@RequestMapping(value = "/exportEmpSalarySummary", method = RequestMethod.GET)
	public RestResponse exportEmpSalarySummary(@RequestParam String startTime,@RequestParam String endTime,@RequestParam List<Integer> firDepartmentIdList, 
			@RequestHeader String token,@RequestParam String flag,HttpServletResponse response) {
		RestResponse result = erpBasePayrollService.exportEmpSalarySummary(startTime,endTime,firDepartmentIdList,token,flag,response);
		return result;
	}
}
