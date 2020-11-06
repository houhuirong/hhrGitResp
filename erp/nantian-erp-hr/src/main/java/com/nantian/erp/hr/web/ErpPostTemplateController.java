package com.nantian.erp.hr.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.service.ErpPostService;
import com.nantian.erp.hr.service.ErpPostTemplateService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value = "岗位模板管理")
@RequestMapping("erp/postTemplate")
public class ErpPostTemplateController {
	
	@Autowired
	private ErpPostService erpPostService;
	
	@Autowired
	private ErpPostTemplateService postTemplateService;
	
	@RequestMapping(value = "/addPostCategory", method = RequestMethod.POST)
	@ApiOperation(value = "新增岗位类别", notes = "新增岗位类别")
	public RestResponse addPost(@RequestBody Map<String, Object> param) {
		return  postTemplateService.insertPostCategory(param);
	}
	
	@RequestMapping(value = "/findPostCategory", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有岗位类别", notes = "查询所有岗位类别")
	public RestResponse findPostCategory() {
		return  postTemplateService.findAllPositionCategory();
	}
	
	@RequestMapping(value = "/findAllJobCategory", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有职位类别", notes = "查询所有职位类别")
	public RestResponse findAllJobCategory(@ApiParam(name="dicCode",value="dicCode",required=true) @RequestParam String dicCode) {
		return  postTemplateService.findAllJobCategory(dicCode);
	}
	
	@RequestMapping(value = "/findAllPostTemplate", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有岗位模板", notes = "查询所有岗位模板")
	public RestResponse findAllPostTemplate(@RequestParam(required=false) String familyId, @RequestParam(required=false) String jobId,
											@RequestParam(required=false) String childId, @RequestParam(required=false) String categoryId,
											@RequestParam(required=false) String postName) {
		return  postTemplateService.findAllPostTemplate(familyId, jobId, childId, categoryId, postName);
	}
	@RequestMapping(value = "/insertPostTemplate", method = RequestMethod.POST)
	@ApiOperation(value = "新增岗位模板", notes = "新增岗位模板")
	public RestResponse insertPostTemplate(@RequestBody Map<String, Object> param) {
		try{
			return  postTemplateService.insertPostTemplate(param);
		}catch (Exception e) {
			return  RestUtils.returnFailure("新增岗位模板异常"+e.getMessage());
		}
		
	}
	@RequestMapping(value = "/findtPostTemplateById", method = RequestMethod.GET)
	@ApiOperation(value = "根据主键查找岗位模板信息", notes = "根据主键查找岗位模板信息")
	public RestResponse findtPostTemplateById(@RequestParam Integer postTemplateId) {
		try {
			return  postTemplateService.findtPostTemplateById(postTemplateId);
		} catch (Exception e) {
			return RestUtils.returnFailure("根据主键查找岗位模板信息异常"+e.getMessage());
		}
		
	}
	
	@RequestMapping(value = "/findtPostChildByJobId", method = RequestMethod.GET)
	@ApiOperation(value = "根据职位类别码值查找职位子类", notes = "根据职位类别码值查找职位子类")
	public RestResponse findtPostChildByJobId(@RequestParam String dicType,@RequestParam String jobId) {
		return  postTemplateService.findtPostChildByJobId(dicType,jobId);
	}
	
	@RequestMapping(value = "/findAllFamily", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有族", notes = "查询所有族")
	public RestResponse findAllFamily() {
		return  postTemplateService.findAllFamily(DicConstants.JOB_FAMILY); 
	}
	
	@RequestMapping(value = "/deletePostTemplateById", method = RequestMethod.GET)
	@ApiOperation(value = "删除岗位模板", notes = "删除岗位模板")
	public RestResponse deletePostTemplateById(@RequestParam Integer postTemplateId) {
		return  postTemplateService.deletePostTemplateById(postTemplateId); 
	}
	
	@RequestMapping(value = "/validatePostionName", method = RequestMethod.GET)
	@ApiOperation(value = "验证岗位名称唯一", notes = "验证岗位名称唯一")
	public RestResponse validatePostionName(@RequestParam String categoryId,@RequestParam String postName) {
		return  postTemplateService.validatePostionName(categoryId,postName); 
	}
	
	@RequestMapping(value = "/findPositionRankList", method = RequestMethod.POST)
	@ApiOperation(value = "根据职位类别、职位子类、职位族类查询职位名称、职级列表", notes = "查询所有岗位类别")
	public RestResponse findPositionRankList(@RequestBody Map<String,Object> params) {
		return  postTemplateService.findPositionRankList(params);
	}
	
}
