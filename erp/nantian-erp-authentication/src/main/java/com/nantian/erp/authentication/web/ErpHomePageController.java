package  com.nantian.erp.authentication.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.authentication.service.ErpHomePageService;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 系统首页controller
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月27日      		ZhangYuWei          1.0       
 * </pre>
 */
@RestController
@Api(value = "系统首页管理接口")
@RequestMapping(value = "/authentication/homePage")
public class ErpHomePageController {
	
	@Autowired
	private ErpHomePageService homePageService;
	
	@ApiOperation(value = "首页-待办事项", notes = "参数")
	@RequestMapping(value = "/findTodoList", method = RequestMethod.GET)
	public RestResponse load(@RequestHeader String token) {
		RestResponse result = homePageService.findTodoList(token);
		return result;
	}
	
}
