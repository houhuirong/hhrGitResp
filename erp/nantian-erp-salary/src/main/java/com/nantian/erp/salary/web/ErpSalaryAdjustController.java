package com.nantian.erp.salary.web;

import com.nantian.erp.common.base.util.RestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.service.ErpSalaryAdjustService;

/**
 * 功能:NT-salary 薪资调整
 * @author hhr
 * @date 2018年09月12日
 * */


@RestController
@Api(value="薪资调整")
@RequestMapping("salary/salaryAdjust")
public class ErpSalaryAdjustController {
	
	private final Logger logger=LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ErpSalaryAdjustService erpSalaryAdjustService;
	@Autowired
	RestTemplate restTemplate;	
	
	@ApiOperation(value = "查询部门及审批人", notes = "参数是：token")
	@RequestMapping(value = "/findDepAndAppr", method = RequestMethod.GET)
	public RestResponse findDepAndAppr(@RequestHeader String token) {
		RestResponse resultList=erpSalaryAdjustService.findDepAndAppr(token);
		return  resultList;
	}

	@ApiOperation(value = "查询部门人员薪酬信息", notes = "参数是：一级部门id")
	@RequestMapping(value = "/findDepMessSalary", method = RequestMethod.GET)
	public RestResponse findDepMessSalary(@RequestHeader String token,@RequestParam Integer id,@RequestParam Integer superLeaderUserId) {
		RestResponse resultList=erpSalaryAdjustService.findDepMessSalary(id,superLeaderUserId,token);
		return  resultList;
	}

	@ApiOperation(value = "填写完信息返回部门人员信息", notes = "参数是：一级部门id")
	@RequestMapping(value = "/backDepMessSalary", method = RequestMethod.POST)
	public RestResponse backDepMessSalary(@RequestBody Map<String,Object> map) {
		RestResponse resultList=erpSalaryAdjustService.backDepMessSalary(map);
		return  resultList;
	}
	
	@ApiOperation(value = "填写完调整薪资表保存到数据库中", notes = "参数是：map")
	@RequestMapping(value = "/saveAdjectMess", method = RequestMethod.POST)
	public RestResponse saveAdjectMess(@RequestBody Map<String,Object> map) {
		RestResponse resultList=erpSalaryAdjustService.saveAdjectMess(map);
		return  resultList;
	}
	
	@ApiOperation(value = "按年份查询调薪审批表", notes = "参数是：map")
	@RequestMapping(value = "/findSalaryApproval", method = RequestMethod.GET)
	public RestResponse findSalaryApproval(@RequestHeader String token,@RequestParam Integer year) {
		RestResponse resultList=erpSalaryAdjustService.findSalaryApproval(token,year);
		return  resultList;
	}
	
	@ApiOperation(value = "调薪审批--填写完信息返回部门人员信息", notes = "参数是：一级部门id")
	@RequestMapping(value = "/backDepMess2Salary", method = RequestMethod.POST)
	public RestResponse backDepMess2Salary(@RequestBody List<Map<String,Object>> list) {
		RestResponse resultList=erpSalaryAdjustService.backDepMess2Salary(list);
		return  resultList;
	}
	
	@ApiOperation(value = "调薪审批--确认调薪", notes = "参数是：人员信息")
	@RequestMapping(value = "/confirmAdjSarlay", method = RequestMethod.POST)
	public RestResponse confirmAdjSarlay(@RequestBody Map<String, Object> map) {
		RestResponse result=erpSalaryAdjustService.confirmAdjSarlay(map);
		return  result;
	}

    @ApiOperation(value = "查询部门人员调薪列表", notes = "参数是：年度、调薪计划名称，一级部门id、部门调薪计划id、审批状态、调薪类型、员工姓名")
    @RequestMapping(value = "/findDepartmentSalaryAdjustList", method = RequestMethod.GET)
    public RestResponse findDepartmentSalaryAdjustList(@RequestHeader String token,
                                                       @RequestParam(required = false) String year,
													   @RequestParam(required = false) String departmentSalaryAdjustPlan,
                                                       @RequestParam(required = false) Integer departmentId,
                                                       @RequestParam(required = false) Integer departmentSalaryAdjustId,
                                                       @RequestParam(required = false) Integer status,
													   @RequestParam(required = false) Integer departmentStatus,
                                                       @RequestParam(required = false) Integer type,
                                                       @RequestParam(required = false) String employeeName,
													   @RequestParam(required = false) Boolean isAllEmployee,
													   @RequestParam(required = false) Boolean isInsert) {
        RestResponse resultList=erpSalaryAdjustService.findDepartmentSalaryAdjustList(year,departmentSalaryAdjustPlan,departmentId,departmentSalaryAdjustId,status,departmentStatus,type,employeeName,isAllEmployee,isInsert,token);
        return  resultList;
    }

    @ApiOperation(value = "保存或提交部门调薪", notes = "参数是：一级部门id")
    @RequestMapping(value = "/saveDepartmentSalaryAdjust", method = RequestMethod.POST)
    public RestResponse saveDepartmentSalaryAdjust(@RequestBody Map<String,Object> departmentSalaryAdjustMap, @RequestHeader String token) {
	    try {
            return erpSalaryAdjustService.saveDepartmentSalaryAdjust(token, departmentSalaryAdjustMap);
        }catch (Exception e){
            logger.error("saveDepartmentSalaryAdjust发生异常 ："+e.getMessage(),e);
            return RestUtils.returnFailure("保存或提交部门调薪失败！");
        }
    }

	@ApiOperation(value = "查询人员调薪详情", notes = "参数是：员工调薪id，员工ID")
	@RequestMapping(value = "/findEmployeeSalaryAdjustInfo", method = RequestMethod.GET)
	public RestResponse findEmployeeSalaryAdjustInfo(@RequestHeader String token, @RequestParam(required = false) Integer salaryAdjustId, @RequestParam(required = false) Integer employeeId) {
		RestResponse resultInfo = erpSalaryAdjustService.findEmployeeSalaryAdjustInfo(salaryAdjustId, employeeId, token);
		return resultInfo;
	}

	@ApiOperation(value = "审批页面确认修改员工调整薪资", notes = "参数是：一级部门id")
	@RequestMapping(value = "/saveEmployeeSalaryAdjust", method = RequestMethod.POST)
	public RestResponse saveEmployeeSalaryAdjust(@RequestBody Map<String,Object> employeeSalaryAdjustMap, @RequestHeader String token) {
		try {
			return erpSalaryAdjustService.saveEmployeeSalaryAdjust(token, employeeSalaryAdjustMap);
		}catch (Exception e){
			logger.error("saveEmployeeSalaryAdjust发生异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("审批页面确认修改员工调整薪资失败！");
		}
	}

	@ApiOperation(value = "审批员工调整薪资", notes = "参数是：一级部门id")
	@RequestMapping(value = "/approveEmployeeSalaryAdjust", method = RequestMethod.POST)
	public RestResponse approveEmployeeSalaryAdjust(@RequestBody Map<String,Object> employeeSalaryAdjustMap, @RequestHeader String token) {
		try {
			return erpSalaryAdjustService.approveEmployeeSalaryAdjust(token, employeeSalaryAdjustMap);
		}catch (Exception e){
			logger.error("approveEmployeeSalaryAdjust发生异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("审批页面审批调整薪资失败！");
		}
	}

	@ApiOperation(value = "确认并导出调整薪资", notes = "参数是：调薪批次id")
	@RequestMapping(value = "/confirmAndExportSalaryAdjust", method = RequestMethod.POST)
	public RestResponse confirmAndExportSalaryAdjust(@RequestBody Map<String,Object> salaryAdjustMap, @RequestHeader String token) {
		try {
			return erpSalaryAdjustService.confirmAndExportSalaryAdjust(token, salaryAdjustMap);
		}catch (Exception e){
			logger.error("confirmAndExportSalaryAdjust发生异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("确认并导出调整薪资失败！");
		}
	}

	@ApiOperation(value = "删除调薪计划", notes = "参数是：调薪批次id")
	@RequestMapping(value = "/deleteDepartmentSalaryAdjust", method = RequestMethod.POST)
	public RestResponse deleteDepartmentSalaryAdjust(@RequestBody Map<String,Object> salaryAdjustMap, @RequestHeader String token) {
		try {
			return erpSalaryAdjustService.deleteDepartmentSalaryAdjust(token, salaryAdjustMap);
		}catch (Exception e){
			logger.error("deleteDepartmentSalaryAdjust发生异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("删除调薪计划失败！");
		}
	}

	//@Scheduled(cron = "0/10 * * * * ?")
	@Scheduled(cron = "0 30 23 * * ?")
	public void automaticAdjustSalaryScheduler() {
		erpSalaryAdjustService.automaticAdjustSalaryScheduler();
	}

	/*@RequestMapping(value="/findEmpInfoByEmail",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	@ApiOperation(value="通过邮箱查询员工基础信息",notes="通过邮箱查询员工基础信息")
	public RestResponse findEmpInfoByEmail(@RequestParam("employeeEmail") String employeeEmail){		
		logger.info("根据员工邮箱查询其部门等基本信息");
		List<Map<String, Object>> resultList=erpSalaryAdjustService.findEmpInfoByEmail(employeeEmail);
		return RestUtils.returnSuccess(resultList);
	}
	
	@RequestMapping(value="/createErpSalaryAdjustTemp",method=RequestMethod.POST)
	@ApiOperation(value="创建调薪申请",notes="创建调薪申请")
	public RestResponse createErpSalaryAdjustFlow(@RequestBody Map<String, Object> map) throws ParseException{
		logger.info("创建调薪申请 参数"+map.toString());
		return RestUtils.returnSuccess(this.erpSalaryAdjustService.createErpSalaryAdjustFlow(map));
	}
	
	@RequestMapping(value="/findSalAdjAllIndividualtApproval",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	@ApiOperation(value="显示所有待我处理",notes="显示所有待我处理")
	public RestResponse findSalAdjAllIndividualtApproval(HttpServletRequest request){
		logger.info("显示所有待我处理"+null);
		List<Map<String, Object>> resultList=erpSalaryAdjustService.findSalAdjAllIndividualtApproval(request);
		return RestUtils.returnSuccess(resultList);
	}
	
	@RequestMapping(value="/createErpSalaryAdjustRecord",method=RequestMethod.POST)
	@ApiOperation(value="调薪申请审批",notes="调薪申请审批")
	public RestResponse ApprovalSalAdjustApply(@RequestBody Map<String,Object> map){
		logger.info("调薪申请审批"+map.toString());
		return RestUtils.returnSuccess(this.erpSalaryAdjustService.createErpSalAdjustRecord(map));
	}
	
	@RequestMapping(value="/findAllSalAdjustTApproval",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	@ApiOperation(value="显示所有调资申请",notes="展示所有调资申请")
	public RestResponse findAllSalAdjustApproval(){
		logger.info("显示待我处理");
		List<Map<String, Object>> resultList=erpSalaryAdjustService.findAllSalAdjustApproval();
		return RestUtils.returnSuccess(resultList);
	}
	
	@RequestMapping(value="/exportSalaryAdjust",method=RequestMethod.POST,produces="application/json;charset=UTF-8")
	@ApiOperation(value="所有调资申请/待我处理-导出",notes="参数：[ [] ]")
	public String exportSalaryAdjust(HttpServletRequest request){
		return this.erpSalaryAdjustService.exportSalaryAdjust(request);
	}*/
}
