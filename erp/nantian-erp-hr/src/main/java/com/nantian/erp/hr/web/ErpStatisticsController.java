package com.nantian.erp.hr.web;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.service.ErpStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 人力数据统计controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月10日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@RequestMapping("erp/statistics")
@Api(value = "人力数据统计")
public class ErpStatisticsController {
	
	@Autowired
	private ErpStatisticsService statisticsService;
	
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	@ApiOperation(value = "人力数据统计", notes = "参数是：年")
	public RestResponse info(@RequestHeader String token, @RequestParam String year) {
		List<Map<String,Object>> infoMap = statisticsService.info(token, year);
		return RestUtils.returnSuccess(infoMap);
	}
	
	@ApiOperation(value = "入职统计导出", notes = "参数是：起止时间")
	@RequestMapping(value = "/exportEntry", method = RequestMethod.GET)
	public RestResponse entryEmport(@RequestParam String startDate,@RequestParam String endDate) {
		RestResponse resultStr = statisticsService.emportXlsxEntry(startDate, endDate);
		return resultStr;
	}

	@ApiOperation(value = "离职统计导出", notes = "参数是：起止时间")
	@RequestMapping(value = "/exportDimission", method = RequestMethod.GET)
	public RestResponse dimissionEmport(@RequestParam String startDate,@RequestParam String endDate) {
		RestResponse resultStr = statisticsService.emportXlsxDimission(startDate, endDate);
		return resultStr;
	}

	@ApiOperation(value = "集团人力数据", notes = "")
	@RequestMapping(value = "/queryGroupData", method = RequestMethod.GET)
	public RestResponse queryGroupData(@RequestHeader String token) {
		RestResponse resultStr = statisticsService.queryGroupData(token);
		return resultStr;
	}
	
	@ApiOperation(value = "一级部门人力数据", notes = "")
	@RequestMapping(value = "/queryFirstDepartmentData", method = RequestMethod.GET)
	public RestResponse queryFirstDepartmentData(@RequestHeader String token, @RequestParam Integer firstDepartmentId) {
		RestResponse resultStr = statisticsService.queryFirstDepartmentData(token, firstDepartmentId);
		return resultStr;
	}
}
