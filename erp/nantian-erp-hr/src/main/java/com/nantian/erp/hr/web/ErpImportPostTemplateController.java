package com.nantian.erp.hr.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpPostTemplateService;
import com.nantian.erp.hr.service.ImportPostTempExcelService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "excel导入教育工作项目经历")
@RequestMapping("erp/util")
public class ErpImportPostTemplateController {
	
	@Autowired
	private ImportPostTempExcelService importPostTempExcelService;
	
	@Autowired
	private ErpPostTemplateService postTemplateService;
	
	
	@RequestMapping(value = "/importPostTemplate", method = RequestMethod.POST)
	@ApiOperation(value = "excel导入岗位模板", notes = "excel岗位模板")
	public RestResponse importPostTemplate(MultipartFile file,HttpServletRequest request) throws IOException {
		String	token=	request.getHeader("token");
		/*if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}*/		
		importPostTempExcelService.readExcelToObj(file);
		return null;
	}
	
	@RequestMapping(value = "/impEducationWorkProjTechnicInfo", method = RequestMethod.POST)
	@ApiOperation(value = "excel导入员工教育工作项目经历，证书等信息", notes = "excel导入员工教育工作项目经历，证书等信息")
	public RestResponse impEducationWorkProjTechnicInfo(MultipartFile file,HttpServletRequest request) throws IOException {
		String	token=	request.getHeader("token");
		/*if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}	*/	
		importPostTempExcelService.readExcelToObj(file);
		return RestUtils.returnSuccessWithString("导入成功");
	}
	
	
	
	
}
