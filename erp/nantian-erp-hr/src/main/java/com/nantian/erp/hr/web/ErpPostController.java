package com.nantian.erp.hr.web;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpPostService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "岗位相关")
@RequestMapping("erp/post")
public class ErpPostController {
	
	@Autowired
	private ErpPostService erpPostService;
	
	@RequestMapping(value = "findAllCategory", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有岗位类别", notes = "查询所有岗位类别")
	public RestResponse findAllCategory() {
		List<Map<String,Object>> allCategory = erpPostService.findAllCategory();
		return RestUtils.returnSuccess(allCategory);
	}
	
}
