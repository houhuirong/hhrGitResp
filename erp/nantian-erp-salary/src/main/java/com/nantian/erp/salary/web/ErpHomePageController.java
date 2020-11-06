package  com.nantian.erp.salary.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.service.ErpHomePageService;

/** 
 * Description: salary工程首页controller
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
@Api(value = "salary工程所有待办事项管理接口")
@RequestMapping(value = "/salary/homePage")
public class ErpHomePageController {
	
	@Autowired
	private ErpHomePageService todoListService;
	
	@ApiOperation(value = "首页-薪酬待办事项", notes = "参数")
	@RequestMapping(value = "/findTodoList", method = RequestMethod.GET)
	public RestResponse load(@RequestHeader String token) {
		RestResponse result = todoListService.findTodoList(token);
		return result;
	}
	
}
