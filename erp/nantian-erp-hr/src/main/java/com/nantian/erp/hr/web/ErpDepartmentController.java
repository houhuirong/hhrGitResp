package com.nantian.erp.hr.web;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.AdminDic;
import com.nantian.erp.hr.data.model.ErpDepartment;
import com.nantian.erp.hr.service.ErpDepartmentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 部门（组织架构、员工信息）Controller
 * @author ZhangYuWei
 */
@RestController
@RequestMapping("erp/department")
@Api(value = "部门相关")
public class ErpDepartmentController {

	@Autowired
	private ErpDepartmentService erpDepartmentService;

	private final Logger logger = LoggerFactory.getLogger(getClass());


	@RequestMapping(value = "addDepartment", method = RequestMethod.POST)
	@ApiOperation(value = "组织架构-新增部门", notes = "新增部门")
	public RestResponse addDepartment(@RequestBody ErpDepartment department) {
		String resultStr = erpDepartmentService.addDepartment(department);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value = "deleteDepartment", method = RequestMethod.GET,produces="application/json;charset=utf-8")
	@ApiOperation(value = "组织架构-删除部门", notes = "删除部门")
	public RestResponse deleteDepartment(@RequestParam Integer departmentId) {
		String resultStr = erpDepartmentService.deleteDepartment(departmentId);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value = "updateDepartment", method = RequestMethod.POST)
	@ApiOperation(value = "组织架构-修改部门", notes = "修改部门")
	public RestResponse updateDepartment(@RequestBody ErpDepartment department , @RequestHeader String token) {
		String resultStr = erpDepartmentService.updateDepartment(department, token);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value = "findAllDepartment", method = RequestMethod.GET)
	@ApiOperation(value = "组织架构-查看全部部门", notes = "查看全部部门")
	public RestResponse findAllDepartment() {
//		List<ErpDepartment> result = erpDepartmentService.findAllDepartment();
		List<Map<String, Object>> result = erpDepartmentService.findAllDepartment();
		return RestUtils.returnSuccess(result);
	}
	
	@RequestMapping(value = "findDepartmentBySelectableId", method = RequestMethod.GET,produces = "application/json;charset=UTF-8")
	@ApiOperation(value = "组织架构-通过部门ID查看一个部门（ID可为空）", notes = "没有参数时，返回所有一级部门；有参数时，返回该一级部门下的全部二级部门")
	public RestResponse findDepartmentBySelectableId(@RequestParam(required=false) Integer departmentId) {
		RestResponse list = erpDepartmentService.findDepartmentBySelectableId(departmentId);
		return list;
	}
	
	@RequestMapping(value = "/findAllFirstDepartmentByPowerParams", method = RequestMethod.POST)
	@ApiOperation(value = "条件查询所有一级部门下的员工信息（供薪酬工程调用）", notes = "参数是：一级部门经理用户编号、部门类别、上级领导；返回结果是三维数组")
	public RestResponse findAllFirstDepartmentByPowerParams(@RequestBody Map<String,Object> params) {
		RestResponse firstDepartmentList = erpDepartmentService.findAllFirstDepartmentByPowerParams(params);
		return firstDepartmentList;
	}

	@RequestMapping(value = "/findSecondDepartmentByPowerParams", method = RequestMethod.POST)
	@ApiOperation(value = "条件查询管辖的二级部门信息", notes = "参数是：token")
	public RestResponse findSecondDepartmentByPowerParams(@RequestBody Map<String,Object> params) {
		RestResponse SecondDepartmentList = erpDepartmentService.findSecondDepartmentByPowerParams(params);
		return SecondDepartmentList;
	}

	@RequestMapping(value = "findAllDepartmentType", method = RequestMethod.GET)
	@ApiOperation(value = "查询岗位类型", notes = "查询岗位类型")
	public RestResponse findAllDepartmentType() {
		List<AdminDic> list = erpDepartmentService.findAllDepartmentType();
		return RestUtils.returnSuccess(list);
	}
	@RequestMapping(value = "addDepartmentType", method = RequestMethod.POST)
	@ApiOperation(value = "添加岗位类型", notes = "添加岗位类型")
	public RestResponse addDepartmentType(@RequestBody Map<String, Object> param) {
		String  departMentTypeName = "";
		if(param.containsKey("departMentTypeName")){
			departMentTypeName = param.get("departMentTypeName").toString();
		}
		return erpDepartmentService.addDepartmentType(departMentTypeName);
	}
	
	@RequestMapping(value = "/findDepartmentByUserID", method = RequestMethod.GET)
	@ApiOperation(value = "根据用户id查找部门", notes = "根据用户id查找部门")
	public RestResponse findDepartmentByUserID(@RequestParam Integer userId) {
		return erpDepartmentService.findDepartmentByUserID(userId);
	}

	
	@RequestMapping(value = "/findDepMessByDepartmentID", method = RequestMethod.GET)
	@ApiOperation(value = "根据审批人查看一级部门下情况", notes = "根据用户id查找相关信息")
	public RestResponse findDepMessByDepartmentID(@RequestParam Integer departmentId){
		return erpDepartmentService.findDepMessByDepartmentID(departmentId);
	}
	
	
	@RequestMapping(value = "/findDepartmentBySLeaderId", method = RequestMethod.GET)
	@ApiOperation(value = "根据审批人查看部门审批情况", notes = "根据用户id查找相关信息")
	public RestResponse findDepartmentBySLeaderId(@RequestParam Integer userId) {
		return erpDepartmentService.findDepartmentBySLeaderId(userId);
	}

	@RequestMapping(value = "/findFirstDepartmentAndSuperLeaderByUserId", method = RequestMethod.GET)
	@ApiOperation(value = "根据登录人查看所管理的一级部门及上级领导列表", notes = "用户id")
	public RestResponse findFirstDepartmentAndSuperLeaderByUserId(@RequestHeader String token) {
		return erpDepartmentService.findFirstDepartmentAndSuperLeaderByUserId(token);
	}
	
	@RequestMapping(value = "/findFirstDepartments", method = RequestMethod.GET)
	@ApiOperation(value = "查找全部一级部门", notes = "")
	public RestResponse findFirstDepartments() {
		return erpDepartmentService.findFirstDepartments();
	}
	
	@RequestMapping(value = "/findFirstDepBySuperById", method = RequestMethod.GET)
	@ApiOperation(value = "通过上级领导查看一级部门", notes = "")
	public RestResponse findFirstDepBySuperById(Integer employeeId) {
		return erpDepartmentService.findFirstDepBySuperById(employeeId);
	}
	
	/* *********************************** 部门调动  *********************************** */
	@PostMapping(value = "/insertTransf")
	@ApiOperation(value = "保存调动申请", notes = "参数是：申请员工信息")
	public RestResponse insertTransf(@RequestBody Map<String,Object> params, @RequestHeader String token){
		try {
			return erpDepartmentService.addgrpTransfApply(params, token);
		}catch (Exception e) {
			logger.error("addgrpTransfApply方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！");
		}
	}
	
	//根据登录人员id查询待其审批的申请
	@PostMapping(value = "/findWaitMsgById")
	@ApiOperation(value = "待我审批申请", notes = "参数是：待我审批申请")
	public RestResponse findWaitMsgById(@RequestBody Map<String,Object> param){
		return erpDepartmentService.findWaitMsgById(param);
		
	}

	//工作调动申请审批-批准
	@PostMapping(value = "/passTrasnfApply")
	@ApiOperation(value = "部门调动申请审批-批准", notes = "参数是：待我审批申请")
	public RestResponse passTrasnfApply(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		try {
			return erpDepartmentService.agreeTransfApply(token, params);
		}catch (Exception e) {
			logger.error("passTrasnfApply方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！");
		}
	}
	
	//工作调动申请审批-驳回
	@PostMapping(value = "/refuseTrasnfApply")
	@ApiOperation(value = "部门调动申请审批-驳回", notes = "参数是：调动申请")
	public RestResponse refuseTrasnfApply(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		try {
			return erpDepartmentService.disagreeTransfApply(token, params);
		}catch (Exception e) {
			logger.error("refuseTrasnfApply方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！");
		}
	}
	
	//工作调动申请审批-撤回
	@PostMapping(value = "/cancleTransfApply")
	@ApiOperation(value = "部门调动申请审批-撤回", notes = "参数是：调动申请")
		public RestResponse cancleTransfApply(@RequestHeader String token,@RequestBody Map<String,Object> params) {
			return erpDepartmentService.cancleTransfApply(token, params);
		}
	
	//根据申请id查询工作调动审批记录
	@GetMapping(value = "/queryTransfRecode")
	@ApiOperation(value = "查询审批记录", notes = "参数是：申请id")
	public RestResponse queryTransfRecode(@RequestParam Integer transferApplyID,@RequestHeader String token){
		return erpDepartmentService.findtransfRecode(transferApplyID, token);

	}
	
	//根据员工id查询其所有申请和审批状态
	@GetMapping(value = "/queryTransfBytoken")
	@ApiOperation(value = "根据员工id查询其所有申请和审批状态", notes = "参数是：员工id")
	public RestResponse queryTransfBytoken(@RequestHeader String token ){
		return erpDepartmentService.findtransfRecodeBytoken(token);
	}
	
	//删除工作调动申请
	@GetMapping(value = "/deleteTransfApply")
	@ApiOperation(value = "删除工作调动", notes = "参数是：申请id")
	public RestResponse deleteTransfApply(@RequestParam Integer id){
	    try {
		    return erpDepartmentService.deleteTransf(id);
        }catch (Exception e) {
            logger.error("deleteTransfApply方法出现异常："+e.getMessage(),e);
            return RestUtils.returnFailure("方法异常，导致操作失败！");
        }
	}

    //批量删除工作调动申请
    @PostMapping(value = "/batchDeleteTransfApply")
    @ApiOperation(value = "批量删除工作调动", notes = "参数是：申请id")
    public RestResponse batchDeleteTransfApply(@RequestBody Map<String,Object> params){
	    try {
            return erpDepartmentService.batchDeleteTransfApply(params);
        }catch (Exception e) {
            logger.error("batchDeleteTransfApply方法出现异常："+e.getMessage(),e);
            return RestUtils.returnFailure("方法异常，导致操作失败！");
        }
    }

	//批量提交工作调动申请
	@PostMapping(value = "/batchSubmitTransfApply")
	@ApiOperation(value = "批量提交工作调动申请", notes = "参数是：申请id")
	public RestResponse batchSubmitTransfApply(@RequestBody Map<String,Object> params, @RequestHeader String token){
		try {
			return erpDepartmentService.batchSubmitTransfApply(params, token);
		}catch (Exception e) {
			logger.error("batchSubmitTransfApply方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！");
		}
	}

	//修改工作调动
	@PostMapping(value = "/adjustTransf")
	@ApiOperation(value = "修改工作调动", notes = "")
	public RestResponse adjustTransf(@RequestBody Map<String,Object> params, @RequestHeader String token) {
		try {
			return erpDepartmentService.updataTranf(params, token);
		}catch (Exception e) {
			logger.error("adjustTransf方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！");
		}
	}
	
	//根据登录人的权限查询所有审批过的申请和审批状态
	@PostMapping(value = "/queryAllTransf")
	@ApiOperation(value = "根据登录人的权限查询所有审批过的和审批状态", notes = "参数是：员工id")
	public RestResponse queryAllTransf(@RequestBody Map<String,Object> params){
		return erpDepartmentService.findAllTransf(params);
	}

	//根据登录人查询本人申请的部门调动列表
	@GetMapping(value = "/queryDepartmentTransfApplyList")
	@ApiOperation(value = "根据登录人查询本人申请的部门调动列表", notes = "参数是：员工id")
	public RestResponse queryDepartmentTransfApplyList(@RequestParam(required = false) Integer oldFirstDepartment, @RequestParam(required = false) Integer newFirstDepartment,
													   @RequestParam(required = false) Integer oldSecDepartment, @RequestParam(required = false) Integer newSecDepartment,
													   @RequestParam(required = false) String employeeName,  @RequestParam(required = false) String startTime,
													   @RequestParam(required = false) String endTime,
													   @RequestHeader String token){
		return erpDepartmentService.queryDepartmentTransfApplyList(oldFirstDepartment, newFirstDepartment, oldSecDepartment, newSecDepartment, employeeName, startTime, endTime, token);
	}

	//
	@GetMapping(value = "/queryDepartmentTransfApplyInfo")
	@ApiOperation(value = "部门调动详情", notes = "参数是：申请id")
	public RestResponse queryDepartmentTransfApplyInfo(@RequestParam Integer id,
													   @RequestHeader String token){
		return erpDepartmentService.queryDepartmentTransfApplyInfo(id, token);
	}

	@PostMapping(value = "/batchSaveTransf")
	@ApiOperation(value = "批量保存调动申请", notes = "参数是：申请员工信息")
	public RestResponse batchSaveTransf(@RequestBody Map<String,Object> params, @RequestHeader String token){
		try {
			return erpDepartmentService.batchSaveTransf(params, token);
		}catch (Exception e){
			logger.error("batchSaveTransf方法出现异常:",e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，查询失败！");
		}
	}
	
	//根据部门权限查询负责的员工
	@PostMapping(value = "/findAllEmployeeByDepartmentUser")
	@ApiOperation(value = "根据部门权限查询负责的员工", notes = "参数是：员工id")
	public RestResponse findAllEmployeeByDepartmentUser(@RequestBody Map<String,Object> params, @RequestHeader String token){
		return erpDepartmentService.findAllEmployeeByDepartmentUser(params, token);
	}
	
	//根据部门权限查询负责的员工
	@PostMapping(value = "/findAllEmployeeByDepartmentAndMonth")
	@ApiOperation(value = "查询部门当月在职的所有员工", notes = "参数是：员工id")
	public RestResponse findAllEmployeeByDepartmentAndMonth(@RequestBody Map<String,Object> params, @RequestHeader String token){
		return erpDepartmentService.findAllEmployeeByDepartmentAndMonth(params, token);
	}

	//根据部门权限查询部门当月在职的所有员工
	@PostMapping(value = "/findAllEmployeeByDepartmentIdAndTime")
	@ApiOperation(value = "查询部门当月在职的所有员工", notes = "参数是：员工id")
	public RestResponse findAllEmployeeByDepartmentIdAndTime(@RequestBody Map<String,Object> params, @RequestHeader String token){
		return erpDepartmentService.findAllEmployeeByDepartmentIdAndTime(params, token);
	}

	//根据当前登录人的角色，获取当前登录的所有一级部门信息和二级部门信息
	@GetMapping(value = "/findFirstAndSeconDeptInfo")
	@ApiOperation(value = "查询当前登录的所有一级部门信息和二级部门信息", notes = "参数是：token")
	public RestResponse findFirstAndSeconDeptInfo(@RequestParam(required = false) Boolean isAllData, @RequestHeader String token){
		return erpDepartmentService.findFirstAndSeconDeptInfo(isAllData, token);
	}

	//查询部门的兄弟部门列表
	@PostMapping(value = "/findBrotherDepartmentList")
	@ApiOperation(value = "查询部门的兄弟部门列表", notes = "参数是：部门id")
	public RestResponse findBrotherDepartmentList(@RequestBody Map<String,Object> params, @RequestHeader String token){
		return erpDepartmentService.findBrotherDepartmentList(params, token);
	}

	@RequestMapping(value = "/findContainSecDepAllFirstDepartment", method = RequestMethod.POST)
	@ApiOperation(value = "条件查询所有包含二级部门的一级部门列表(供薪酬工程调用)", notes = "参数是：一级部门经理用户编号、部门类别、上级领导；返回结果是三维数组")
	public RestResponse findContainSecDepAllFirstDepartment(@RequestBody Map<String,Object> params) {
		RestResponse firstDepartmentList = erpDepartmentService.findContainSecDepAllFirstDepartment(params);
		return firstDepartmentList;
	}

	@RequestMapping(value = "/findFirstDepartmentCountMap", method = RequestMethod.GET)
	@ApiOperation(value = "获取各个一级部门员工数量", notes = "参数是：无")
	public RestResponse findFirstDepartmentCountMap() {
		return erpDepartmentService.findFirstDepartmentCountMap();
	}

	/**
	 * 定时任务：每天凌晨12.10执行修改审批通过的工作调动申请的员工项目组id
	 */
	@Scheduled(cron = "0 0 1 * * ?")
	public void antuoUpdateEmpDepInfo(){
		erpDepartmentService.antuoUpdateEmpEmpDepInfochedule();
	}
	
	@RequestMapping(value="/findAllFirstDepartmentBySecDep",method=RequestMethod.POST)
	@ApiOperation(value="获取二级部门经理管理",notes="参数[]")
	public RestResponse findAllFirstDepartmentBySecDep(@RequestBody Map<String,Object> params){
		return erpDepartmentService.findAllFirstDepartmentBySecDep(params);
	}

//	/**
//	 * 定时任务：每年5月28号 5点修复部门调动错误数据
//	 */
//	@Scheduled(cron = "0 0 5 28 5 ?")
//	public void antuoModifyCurrentPerson(){
//		try{
//			erpDepartmentService.modifyCurrentPerson();
//		}catch (Exception e){
//			logger.info("antuoModifyCurrentPerson出错"+e, e);
//		}
//
//	}

}
