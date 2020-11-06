package com.nantian.erp.hr.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpImportExcelService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/erp/importExcel")
@Api(value = "人力资源-部门-员工信息导入")
public class ErpImportExcelController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ErpImportExcelService erpImportExcelService;
	
	/*@ApiOperation(value = "员工信息导入", notes = "参数：null", httpMethod = "POST")
	@RequestMapping(value = "/importEmpExcel", method = RequestMethod.POST)
	public RestResponse importEmpExcel(MultipartFile file,HttpServletRequest request) throws IOException {
		logger.info("员工信息导入  参数： {[ null ]}" );
		String	token=	request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}		
		
		return this.erpImportExcelService.importEmpExcel(file,token);
	}*/
	
	@ApiOperation(value = "员工信息导入", notes = "员工信息")
	@RequestMapping(value = "/importEmpExcel", method = RequestMethod.POST)
	public RestResponse importEmpExcel(HttpServletRequest request) throws IOException {
		logger.info("员工信息导入  参数： {[ null ]}" );
		String	token=	request.getHeader("token");
		if (!StringUtils.isNotBlank(token)) {
			return RestUtils.returnSuccessWithString("未携带token");
		}
		
		MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
		Iterator<String> iter = multiRequest.getFileNames();
		RestResponse response = null; 
        while(iter.hasNext())
        {
            //一次遍历所有文件
            MultipartFile file=multiRequest.getFile(iter.next().toString());
            if(file!=null)
            {
//                String path="E:/springUpload"+file.getOriginalFilename();
                //上传
//                file.transferTo(new File(path));
            	response =  this.erpImportExcelService.importEmpExcel(file,token);
            }
             
        }
	return  response;
	}
	
	@ApiOperation(value = "模糊查询", notes = "员工信息")
	@RequestMapping(value = "/fuzzyQueryEmp", method = RequestMethod.POST)
	public RestResponse fuzzyQueryEmp(@RequestBody Map<String,	Object> map) throws IOException {
		return erpImportExcelService.fuzzyQueryEmp(map);
	}
}
