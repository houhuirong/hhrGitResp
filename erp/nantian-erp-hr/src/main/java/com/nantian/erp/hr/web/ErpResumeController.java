package com.nantian.erp.hr.web;

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
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpResume;
import com.nantian.erp.hr.service.ErpResumeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 简历controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月05日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("erp/resume")
@Api(value = "简历")
public class ErpResumeController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ErpResumeService erpResumeService;
	
	@RequestMapping(value = "/addResume", method = RequestMethod.POST)
	@ApiOperation(value = "新增简历", notes = "新增简历")
	public RestResponse addResume(@RequestHeader String token,MultipartFile file, ErpResume resume) {
		RestResponse result = erpResumeService.insertResume(token,resume,file);
		return result;
	}
	
	@RequestMapping(value = "/updateResume",method = RequestMethod.POST)
	@ApiOperation(value = "简历修改", notes = "简历修改")
	public RestResponse updateResume(@RequestHeader String token,MultipartFile file, ErpResume resume) {
		RestResponse result = erpResumeService.updateResume(token,resume,file);
		return result;
	}
	
	@RequestMapping(value = "/queryValidResume", method = RequestMethod.GET)
	@ApiOperation(value = "查询有效简历", notes = "查询有效简历")
	public RestResponse queryValidResume(@RequestParam Boolean isTrainee) {
		RestResponse mapList = erpResumeService.queryValidResume(isTrainee);
		return mapList;
	}
	
	@RequestMapping(value = "/queryArchivedResume", method = RequestMethod.GET)
	@ApiOperation(value = "查询已归档简历", notes = "查询已归档简历")
	public RestResponse queryArchivedResume(@RequestParam Boolean isTrainee,@RequestParam Integer page,@RequestParam Integer rows,@RequestParam(required=false) String keyword) {
		RestResponse map = erpResumeService.queryArchivedResume(isTrainee, page, rows, keyword);
		return map;
	}
	
	@RequestMapping(value = "/queryRecommendedResume", method = RequestMethod.GET)
	@ApiOperation(value = "可推荐的简历列表", notes = "参数是：是否是实习生")
	public RestResponse queryRecommendedResume(@RequestParam Boolean isTrainee) {
		RestResponse resumeList = erpResumeService.queryRecommendedResume(isTrainee);
		return resumeList;
	}
	
	@RequestMapping(value = "/invalidResume", method = RequestMethod.GET)
	@ApiOperation(value = "简历失效", notes = "简历失效")
	public RestResponse invaildResume(@RequestParam Integer resumeId,@RequestParam String reason,@RequestHeader String token) {
		try {
			String resultStr = erpResumeService.invaildResume(resumeId,reason,token);
			return RestUtils.returnSuccessWithString(resultStr);
		} catch (Exception e) {
			logger.error("invaildResume方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致没有使简历失效！");
		}
	}
	
	@RequestMapping(value = "/validResume", method = RequestMethod.GET)
	@ApiOperation(value = "简历生效", notes = "简历生效")
	public RestResponse vaildResume(@RequestParam Integer resumeId,@RequestParam String reason,@RequestHeader String token) {
		String resultStr = erpResumeService.vaildResume(resumeId,reason,token);
		return RestUtils.returnSuccessWithString(resultStr);
	}
	
	@RequestMapping(value = "/findInterviewRecord", method = RequestMethod.GET)
	@ApiOperation(value = "根据简历ID查询面试记录", notes = "根据简历ID查询面试记录")
	public RestResponse findInterviewRecord(@RequestParam Integer resumeId) {
		RestResponse interviewRecordList = erpResumeService.findInterviewRecord(resumeId);
		return interviewRecordList;
	}
	
	@RequestMapping(value = "/findResumeDetail", method = RequestMethod.GET)
	@ApiOperation(value = "根据简历id查询简历", notes = "根据简历id查询简历")
	public RestResponse findResumeDetail(@RequestParam Integer resumeId) {
		RestResponse resumeInfo = erpResumeService.findResumeDetail(resumeId);
		return resumeInfo;
	}
	
	@RequestMapping(value = "/downloadResume", method = RequestMethod.GET)
	@ApiOperation(value = "简历下载", notes = "简历下载")
	public RestResponse downloadResume(@RequestParam Integer resumeId) {
		RestResponse result = erpResumeService.downloadResume(resumeId);
		return result;
	}
	
	@RequestMapping(value = "/previewResume", method = RequestMethod.GET)
	@ApiOperation(value = "预览简历", notes = "预览简历")
	public RestResponse previewResume(@RequestHeader String token,@RequestParam Integer resumeId) {
		RestResponse result = erpResumeService.previewResume(token,resumeId);
		return result;
	}
	
	@RequestMapping(value = "/validPhoneAndEmail", method = RequestMethod.POST)
	@ApiOperation(value = "新增简历时校验手机号码、邮箱", notes = "参数是：phone或email")
	public RestResponse validPhoneAndEmail(@RequestBody Map<String,Object> params) {
		RestResponse result = erpResumeService.validPhoneAndEmail(params);
		return result;
	}
	
}
