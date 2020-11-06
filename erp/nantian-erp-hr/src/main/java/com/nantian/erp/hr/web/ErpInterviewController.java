package com.nantian.erp.hr.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpInterviewService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 面试controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月06日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("erp/interview")
@Api(value = "面试")
public class ErpInterviewController {
	
	@Autowired
	private ErpInterviewService erpInterviewService;
	
	/* **************************************** 简历筛选 **************************************** */
	
	@RequestMapping(value="/resumeFilterQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "简历筛选-查询列表", notes = "参数是：token")
	public RestResponse resumeFilterQueryList(@RequestHeader String token) {
		RestResponse result = erpInterviewService.resumeFilterQueryList(token);
		return result;
	}
	
	@RequestMapping(value="/resumeFilterPass",method = RequestMethod.POST)
	@ApiOperation(value = "简历筛选-通过", notes = "参数是：token、面试预约信息")
	public RestResponse resumeFilterPass(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.resumeFilterPass(token,params);
		return result;
	}
	
	@RequestMapping(value="/resumeFilterNoPass",method = RequestMethod.POST)
	@ApiOperation(value = "简历筛选-不通过", notes = "参数是：token、面试记录")
	public RestResponse resumeFilterNoPass(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.resumeFilterNoPass(token,params);
		return result;
	}
	
	/* **************************************** 预约面试 **************************************** */
	
	@RequestMapping(value="/interviewOrderQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "预约面试-查询列表", notes = "参数是：token")
	public RestResponse interviewOrderQueryList(@RequestHeader String token) {
		RestResponse result = erpInterviewService.interviewOrderQueryList(token);
		return result;
	}
	
	@RequestMapping(value="/interviewOrderQueryOrder",method = RequestMethod.GET)
	@ApiOperation(value = "预约面试-通过面试Id查询面试预约信息", notes = "参数是：面试流程Id")
	public RestResponse interviewOrderQueryOrder(@RequestParam Integer interviewId) {
		RestResponse result = erpInterviewService.interviewOrderQueryOrder(interviewId);
		return result;
	}
	
	@RequestMapping(value="/interviewOrderQueryOrderRecord",method = RequestMethod.GET)
	@ApiOperation(value = "预约面试-通过面试Id查询面试预约记录", notes = "参数是：面试流程Id")
	public RestResponse interviewOrderQueryOrderRecord(@RequestParam Integer interviewId) {
		RestResponse result = erpInterviewService.interviewOrderQueryOrderRecord(interviewId);
		return result;
	}
	
	@RequestMapping(value="/interviewOrderSuccess",method = RequestMethod.POST)
	@ApiOperation(value = "预约面试-成功", notes = "参数是：token、面试预约信息")
	public RestResponse interviewOrderSuccess(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.interviewOrderSuccess(token,params);
		return result;
	}
	
	@RequestMapping(value="/interviewOrderAgain",method = RequestMethod.POST)
	@ApiOperation(value = "预约面试-再联系", notes = "参数是：面试预约记录")
	public RestResponse interviewOrderAgain(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.interviewOrderAgain(token,params);
		return result;
	}
	
	@RequestMapping(value="/interviewOrderFailure",method = RequestMethod.POST)
	@ApiOperation(value = "预约面试-失败", notes = "参数是：token、面试记录")
	public RestResponse interviewOrderFailure(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.interviewOrderFailure(token,params);
		return result;
	}
	
	/* **************************************** 待我面试 **************************************** */
	
	@RequestMapping(value="/waitingForMeQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "待我面试-查询列表", notes = "参数是：token")
	public RestResponse waitingForMeQueryList(@RequestParam(required = false) Integer pageType, @RequestHeader String token) {
		RestResponse result = erpInterviewService.waitingForMeQueryList(pageType, token);
		return result;
	}
	
	@RequestMapping(value="/waitingForMeQueryPositionRankList",method = RequestMethod.GET)
	@ApiOperation(value = "待我面试-社招生复试职位职级下拉菜单查询", notes = "参数是：岗位Id")
	public RestResponse waitingForMeQueryPositionRankList(@RequestParam Integer postId) {
		RestResponse result = erpInterviewService.waitingForMeQueryPositionRankList(postId);
		return result;
	}
	
	@RequestMapping(value="/waitingForMeQueryReexamInfo",method = RequestMethod.GET)
	@ApiOperation(value = "待我面试-社招生复试信息查询", notes = "参数是：面试Id")
	public RestResponse waitingForMeQueryReexamInfo(@RequestParam Integer interviewId) {
		RestResponse result = erpInterviewService.waitingForMeQueryReexamInfo(interviewId);
		return result;
	}
	
	@RequestMapping(value="/waitingForMeSocialFirstJudge",method = RequestMethod.POST)
	@ApiOperation(value = "待我面试-社招生初试评价", notes = "参数是：token、面试记录PO")
	public RestResponse waitingForMeSocialFirstJudge(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.waitingForMeSocialFirstJudge(token,params);
		return result;
	}
	
	@RequestMapping(value="/waitingForMeSocialReexamJudge",method = RequestMethod.POST)
	@ApiOperation(value = "待我面试-社招生复试评价", notes = "参数是：token、复试信息PO")
	public RestResponse waitingForMeSocialReexamJudge(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.waitingForMeSocialReexamJudge(token,params);
		return result;
	}
	
	@RequestMapping(value="/waitingForMeSocialReexamJudgeSave",method = RequestMethod.POST)
	@ApiOperation(value = "待我面试-社招生复试评价-保存", notes = "参数是：token、复试信息PO")
	public RestResponse waitingForMeSocialReexamJudgeSave(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.waitingForMeSocialReexamJudgeSave(token,params);
		return result;
	}
	
	@RequestMapping(value="/waitingForMeTraineeJudge",method = RequestMethod.POST)
	@ApiOperation(value = "待我面试-实习生面试评价", notes = "参数是：token、面试记录PO")
	public RestResponse waitingForMeTraineeJudge(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.waitingForMeTraineeJudge(token,params);
		return result;
	}
	
	/* **************************************** 面试不通过 **************************************** */
	
	@RequestMapping(value="/interviewNoPassQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "面试不通过-查询列表", notes = "参数是：")
	public RestResponse interviewNoPassQueryList() {
		RestResponse result = erpInterviewService.interviewNoPassQueryList();
		return result;
	}
	
	@RequestMapping(value="/interviewNoPassValid",method = RequestMethod.POST)
	@ApiOperation(value = "面试不通过-可推荐", notes = "参数是：token、面试记录")
	public RestResponse interviewNoPassValid(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.interviewNoPassValid(token,params);
		return result;
	}
	
	@RequestMapping(value="/interviewNoPassInvalid",method = RequestMethod.POST)
	@ApiOperation(value = "面试不通过-归档", notes = "参数是：token、面试记录")
	public RestResponse interviewNoPassInvalid(@RequestHeader String token,@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.interviewNoPassInvalid(token,params);
		return result;
	}
	
	/* **************************************** 所有面试 **************************************** */
	
	@RequestMapping(value="/allInterviewQueryList",method = RequestMethod.GET)
	@ApiOperation(value = "所有进行中面试-查询列表", notes = "参数是：")
	public RestResponse allInterviewQueryList(@RequestHeader String token) {
		RestResponse result = erpInterviewService.allInterviewQueryList(token);
		return result;
	}
	
	/* **************************************** 未来的字典管理工具 **************************************** */
	
	@RequestMapping(value="/adminDicInsertContact",method = RequestMethod.POST)
	@ApiOperation(value = "字典管理-新增面试联系人", notes = "参数是：联系人姓名、电话")
	public RestResponse adminDicInsertContact(@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.adminDicInsertContact(params);
		return result;
	}
	
	@RequestMapping(value="/adminDicInsertPlace",method = RequestMethod.POST)
	@ApiOperation(value = "字典管理-新增面试地点", notes = "参数是：面试地点")
	public RestResponse adminDicInsertPlace(@RequestBody Map<String,Object> params) {
		RestResponse result = erpInterviewService.adminDicInsertPlace(params);
		return result;
	}
	
	@RequestMapping(value="/adminDicQueryAll",method = RequestMethod.GET)
	@ApiOperation(value = "字典管理-查询全部的面试方式、联系人、面试地点", notes = "无参数")
	public RestResponse adminDicQueryAll() {
		RestResponse result = erpInterviewService.adminDicQueryAll();
		return result;
	}
	
}
