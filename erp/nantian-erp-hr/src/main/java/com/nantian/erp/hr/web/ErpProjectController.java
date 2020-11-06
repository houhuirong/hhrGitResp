package com.nantian.erp.hr.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.model.ErpProjectInfo;
import com.nantian.erp.hr.service.ErpProjectService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("erp/project")
@Api(value = "项目相关")
public class ErpProjectController {
	
	@Autowired
	private ErpProjectService erpProjectService;
	
	@RequestMapping(value = "findproject", method = RequestMethod.GET)
	@ApiOperation(value = "查询项目", notes = "查询项目")
	public RestResponse findProject(){
		List<ErpProjectInfo> list=erpProjectService.findAll();
		return RestUtils.returnSuccess(list);
	}
	
}
