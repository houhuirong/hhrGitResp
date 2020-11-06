package com.nantian.erp.email.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.email.service.ConvertFormatService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 文件格式转换controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月18日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("format/convert")
@Api(value = "文件格式转换")
public class ConvertFormatController {
	@Autowired
	private ConvertFormatService convertFormatService;
	
	@RequestMapping(value = "/wordToPdfDemo", method = RequestMethod.POST)
	@ApiOperation(value = "word转pdf", notes = "无参数")
	public RestResponse wordToPdfDemo() {
		return convertFormatService.wordToPdfDemo();
	}
	
	@RequestMapping(value = "/wordToHtmlDemo", method = RequestMethod.POST)
	@ApiOperation(value = "word转html", notes = "无参数")
	public RestResponse wordToHtmlDemo() {
		return convertFormatService.wordToHtmlDemo();
	}
	
	@RequestMapping(value = "/wordToPdf", method = RequestMethod.POST)
	@ApiOperation(value = "word转pdf", notes = "参数是：word文件路径、pdf文件路径")
	public RestResponse wordToPdf(@RequestBody Map<String,String> params) {
		return convertFormatService.wordToPdf(params);
	}
	
	@RequestMapping(value = "/wordToHtml", method = RequestMethod.POST)
	@ApiOperation(value = "word转html", notes = "参数是参数是：word文件路径、pdf文件路径")
	public RestResponse wordToHtml(@RequestBody Map<String,String> params) {
		return convertFormatService.wordToHtml(params);
	}
	
}
