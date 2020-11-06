package com.nantian.erp.hr.web;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.vo.ErpEmployeeQueryVo;
import com.nantian.erp.hr.data.vo.ErpPositionQueryParamVO;
import com.nantian.erp.hr.service.ErpPostApplyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("erp/positionApply")
@Api(value = "岗位申请")
public class ErpPostApplyController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	ErpPostApplyService postApplyService;
	
	@RequestMapping(value = "/getPostApplyInfo", method = RequestMethod.GET)
	@ApiOperation(value = "获取岗位申请信息", notes = "获取岗位申请信息")
	public RestResponse addPostApplyInfo(@RequestParam Integer id, @RequestParam(name = "firstDepid", required = false) Integer firstDepid ){
		return postApplyService.getPostApplyInfo(id,firstDepid);
	}
	
	
	@RequestMapping(value = "/findPositionDutyAndRequire", method = RequestMethod.GET)
	@ApiOperation(value = "根据职位类别和岗位名称 定位 岗位职责,岗位要求", notes = "根据职位类别和岗位名称 定位 岗位职责,岗位要求")
	public RestResponse findPositionDutyAndRequire(String categoryId, String postName){
		return postApplyService.findPositionDutyAndRequire(categoryId, postName);
	}
	
	@RequestMapping(value = "/findAllPositionCategoryInApply", method = RequestMethod.GET)
	@ApiOperation(value = "从字典获取所有的岗位类别", notes = "从字典获取所有的岗位类别")
	public RestResponse findAllPositionCategory (){
		return postApplyService.findAllPositionCategory();
	}

	@RequestMapping(value = "/findPositionName", method = RequestMethod.GET)
	@ApiOperation(value = "通过岗位类别查询岗位名称", notes = "通过岗位类别查询岗位名称")
	public RestResponse findPositionName (@RequestParam String categoryId){
		return postApplyService.findPositionName(categoryId);
	}
	
	//添加岗位申请信息
	@RequestMapping(value = "/addPostApplyInfo", method = RequestMethod.POST)
	@ApiOperation(value = "添加岗位申请信息", notes = "添加岗位申请信息")
	public RestResponse addPostApplyInfo (@RequestHeader String token,@RequestBody Map<String, Object> param){
		try {
			return postApplyService.addPostApplyInfo(param, token);
		} catch (Exception e) {
			logger.error("添加岗位申请信息异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("添加岗位申请信息异常:"+e.getMessage());
		}
	}
	//根据当前登录人查询待审批的岗位申请
	@RequestMapping(value = "/getApprovalPendingByLogin", method = RequestMethod.GET)
	@ApiOperation(value = "根据当前登录人查询待审批的岗位申请", notes = "根据当前登录人查询待审批的岗位申请")
	public RestResponse getApprovalPendingByLogin (@RequestParam Map<String,Object> map){
		return postApplyService.getApprovalPendingByLogin(map);
	}
	
	//待审批的岗位申请中驳回操作
	@RequestMapping(value = "/rebutApply", method = RequestMethod.POST)
	@ApiOperation(value = "待审批的岗位申请中驳回操作", notes = "待审批的岗位申请中驳回操作")
	public RestResponse rebutApply (@RequestBody Map<String, Object> param){
		return postApplyService.rebutApply(param);
	}
	@RequestMapping(value = "/agreeApply", method = RequestMethod.GET)
	@ApiOperation(value = "待审批的岗位申请中批准操作", notes = "待审批的岗位申请中批准操作")
	public RestResponse agreeApply (@RequestParam Integer postId){
		return postApplyService.agreeApply(postId);
	}
	
	
	@RequestMapping(value = "/getMyApplyPostionByLogin", method = RequestMethod.GET)
	@ApiOperation(value = "根据当前登录人查询我申请的岗位", notes = "根据当前登录人查询我申请的岗位")
	public RestResponse getMyApplyPostionByLogin (@RequestParam Map<String,Object> map){
		return postApplyService.getMyApplyPostionByLogin(map);
	}
	
	@RequestMapping(value = "/updateMyApplyPostionByLogin", method = RequestMethod.POST)
	@ApiOperation(value = "根据当前登录人修改我申请的岗位", notes = "根据当前登录人修改我申请的岗位")
	public RestResponse updateMyApplyPostionByLogin (@RequestHeader String token,@RequestBody Map<String, Object> param){
		
		try {
			return postApplyService.updateMyApplyPostionByLogin(param, token);
		} catch (Exception e) {
			logger.error("根据当前登录人修改我申请的岗位异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("updateMyApplyPostionByLogin()"+e.getMessage());
		}
		
	}
	@RequestMapping(value = "/getMyPostionApplyByPostId", method = RequestMethod.GET)
	@ApiOperation(value = "根据岗位申请表主键获取当前岗位申请信息", notes = "根据岗位申请表主键获取当前岗位申请信息")
	public RestResponse getMyPostionApplyByPostId (@RequestParam Integer id,@RequestParam Integer postId){
		return postApplyService.getMyPostionApplyByPostId(id,postId);
	}
	
/*	@RequestMapping(value = "/rebackOperation", method = RequestMethod.GET)
	@ApiOperation(value = "申请人执行撤回操作", notes = "申请人执行撤回操作")
	public RestResponse rebackOperation (@RequestParam Integer id,
			@RequestParam String content,@RequestParam Integer postId){
		return postApplyService.rebackOperation(id,content,postId);
	}*/
	@RequestMapping(value = "/rebackOperation", method = RequestMethod.POST)
	@ApiOperation(value = "申请人执行撤回操作", notes = "申请人执行撤回操作")
	public RestResponse rebackOperation (@RequestBody Map<String, Object> param){
		return postApplyService.rebackOperation(param);
	}
	
	@RequestMapping(value = "/deleteMyApplyOperation", method = RequestMethod.GET)
	@ApiOperation(value = "申请人执行删除操作", notes = "申请人执行删除操作")
	public RestResponse deleteApplyOperation (@RequestParam Integer postId){
		return postApplyService.deleteMyApplyOperation(postId);
	}
	
	@RequestMapping(value = "/findAllPublishPosition", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有发布中的岗位", notes = "查询所有发布中的岗位")
	public RestResponse findAllPublishPostionApplyn (@RequestHeader String token){
		return postApplyService.findAllPublishPostionApplyn(token);
	}
	
	@RequestMapping(value = "/recommendedResume", method = RequestMethod.GET)
	@ApiOperation(value = "推荐简历", notes = "推荐简历")
	public RestResponse recommendedResume (@RequestHeader String token, @RequestParam Integer postId,
			@RequestParam  Integer resumeId,@RequestParam  Boolean isTrainee){
		return postApplyService.recommendedResume(token, postId,resumeId,isTrainee); //Boolean isTrainee
	}
	
	@RequestMapping(value = "/startInterview", method = RequestMethod.POST)
	@ApiOperation(value = "发起面试", notes = "发起面试")
	public RestResponse startInterview (@RequestHeader String token, @RequestBody Map<String,Object> params){
		return postApplyService.startInterview(token, params);
	}
	
	@RequestMapping(value = "/findAllClosedPosition", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有已关闭的岗位", notes = "查询所有已关闭的岗位")
	public RestResponse findAllClosedPosition (){
		return postApplyService.findAllClosedPosition();
	}
	
	@RequestMapping(value = "/findApprovePersonInDic", method = RequestMethod.GET)
	@ApiOperation(value = "查询审批人总裁", notes = "查询审批人总裁")
	public RestResponse findApprovePersonInDic (){
		return postApplyService.findApprovePersonInDic();
	}
	
	@RequestMapping(value = "/deleteRequireOrDuty", method = RequestMethod.GET)
	@ApiOperation(value = "删除岗位职责或要求", notes = "查询审批人总裁")
	public RestResponse deleteRequireOrDuty (@RequestParam Integer id, @RequestParam String flag){
		
		return postApplyService.deleteRequireOrDuty( id, flag);
		
//		return RestUtils.returnSuccess("success");
	}
	
   /**
    * 
    * @param postId  岗位申请ID
    * @param num  1 查询总面试,2 查询已发offer ,3 查询已入职
    * @return  查询总面试,查询已发offer,查询已入职
    */
	@RequestMapping(value = "/findRecruitInfoById", method = RequestMethod.GET)
	@ApiOperation(value = "根据该岗位id查询所有招聘信息", notes = "参数是：岗位ID、类别；返回：简历列表")
	public RestResponse findInterviewerById(@RequestParam Integer postId, @RequestParam Integer num) {
		switch (num) {
			case 1:
				List<Map<String, Object>> map1 = postApplyService.findInterviewerById(postId);
				//String jsonString = JSON.toJSONString(map,SerializerFeature.WriteMapNullValue);
				return RestUtils.returnSuccess(map1);
			case 2:
				List<Map<String, Object>> map2 = postApplyService.findOfferedById(postId);
				//jsonString = JSON.toJSONString(map2,SerializerFeature.WriteMapNullValue);
				return RestUtils.returnSuccess(map2);
			case 3:
				List<Map<String, Object>> map3 = postApplyService.findEntriedById(postId);
				//jsonString = JSON.toJSONString(map3,SerializerFeature.WriteMapNullValue);
				return RestUtils.returnSuccess(map3);
		}
		return RestUtils.returnSuccessWithString("num无效");
	}
	
	@RequestMapping(value = "/findAllSecondDepartmentByFirDepId", method = RequestMethod.GET)
	@ApiOperation(value = "根据一级部门ID查找所有二级部门", notes = "参数是一级部门ID")
	public RestResponse findInterviewerById(@RequestParam Integer firstDepId) {
		List<Map<String,Object>> list = null;
		try {
			list =  postApplyService.findAllSecondDepartmentByFirDepId(firstDepId);
		} catch (Exception e) {
			return RestUtils.returnFailure("根据一级部门ID查找所有二级部门报错:"+e.getMessage());
		}
		
		return RestUtils.returnSuccess(list);
	}
	
	@RequestMapping(value="/changeStatus",method = RequestMethod.GET)
	@ApiOperation(value = "改变岗位状态", notes = "改变岗位状态")
	public RestResponse changeStatus(@RequestParam String operType, @RequestParam Integer postId,@RequestParam String reason,@RequestParam Integer id){
		String resultString = postApplyService.changeStatus(operType, postId,reason,id);
		return RestUtils.returnSuccessWithString(resultString);
	}
	
	@RequestMapping(value = "/findAllPositionList", method = RequestMethod.POST)
	@ApiOperation(value = "查询所有岗位列表", notes = "查询所有岗位列表")
	public RestResponse findAllPositionList (@RequestBody ErpPositionQueryParamVO erpPositionQueryParamVO
//			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
//			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows
			){
		return postApplyService.findAllPositionList(erpPositionQueryParamVO);
	}
	
	@RequestMapping(value = "/findPositionDetail", method = RequestMethod.GET)
	@ApiOperation(value = "查询岗位详情", notes = "查询岗位详情")
	public RestResponse findPositionDetail (@RequestParam Integer postId){
		return postApplyService.findPositionDetail(postId);
	}
	
	@RequestMapping(value = "/findLevelPriorityList", method = RequestMethod.GET)
	@ApiOperation(value = "查询优先级列表", notes = "查询优先级列表")
	public RestResponse findLevelPriorityList (){
		return postApplyService.findLevelPriorityList();
	}
	
	@RequestMapping(value = "/findReasonRecruitList", method = RequestMethod.GET)
	@ApiOperation(value = "查询招聘原因列表", notes = "查询招聘原因列表")
	public RestResponse findReasonRecruitList (){
		return postApplyService.findReasonRecruitList();
	}
	
	@RequestMapping(value = "/updateHrCharge", method = RequestMethod.POST)
	@ApiOperation(value = "修改HR负责人", notes = "修改HR负责人")
	public RestResponse updateHrCharge (@RequestBody ErpPositionQueryParamVO erpPositionQueryParamVO){
		return postApplyService.updateHrCharge(erpPositionQueryParamVO);
	}

	@RequestMapping(value = "/exportAllPositionList", method = RequestMethod.POST)
	@ApiOperation(value = "导出所有岗位列表", notes = "导出所有岗位列表")
	public RestResponse exportAllPositionList (@RequestBody ErpPositionQueryParamVO erpPositionQueryParamVO){
		return postApplyService.exportAllPositionList(erpPositionQueryParamVO);
	}
}