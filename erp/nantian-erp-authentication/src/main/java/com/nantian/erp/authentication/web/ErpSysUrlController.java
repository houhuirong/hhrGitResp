package  com.nantian.erp.authentication.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nantian.erp.authentication.data.vo.ErpSysUrlVo;
import com.nantian.erp.authentication.service.ErpSysUrlService;
import com.nantian.erp.common.rest.RestResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 模块-url管理接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月11日                   caoxiubin         1.0        
 * </pre>
 */
@RestController
@Api(value = "模块-url信息管理接口")
@RequestMapping(value = "/authentication/url")
public class ErpSysUrlController {
	
	@Autowired
	private ErpSysUrlService erpSysUrlService;
	
	@ApiOperation(value = "模块-url管理-查询模块-url列表", notes = "参数 [[ ]]", httpMethod="GET")
	@RequestMapping(value = "/findAllUrlToModul", method = RequestMethod.GET)
	public RestResponse findAllUrlToModul() {
		return this.erpSysUrlService.findAllUrlToModul();
	}
	
	@ApiOperation(value = "模块-url管理-新增url信息", notes = "参数 [[ url信息 ]]", httpMethod="POST")
	@RequestMapping(value = "/insertUrl", method = RequestMethod.POST)
	public RestResponse insertUrl(@RequestBody ErpSysUrlVo erpSysUrlVo,HttpServletRequest request) {
		return this.erpSysUrlService.insertUrl(erpSysUrlVo,request);
	}

	@ApiOperation(value = "模块-url管理-修改url信息", notes = "参数 [[ url信息 ]]", httpMethod="POST")
	@RequestMapping(value = "/updateUrl", method = RequestMethod.POST)
	public RestResponse updateUrl(@RequestBody ErpSysUrlVo erpSysUrlVo) {
		return this.erpSysUrlService.updateUrl(erpSysUrlVo);
	}
	
	@ApiOperation(value = "模块-url管理-删除url信息", notes = "参数 [[ 角色ID ]]", httpMethod="GET")
	@RequestMapping(value = "/deleteUrl", method = RequestMethod.GET)
	public RestResponse deleteUrl(@RequestParam Integer UrlID) {
		return this.erpSysUrlService.deleteUrl(UrlID);
	}
	
	@ApiOperation(value="模块-url管理-判断输入的url路径是否存在", notes = "参数是：url对象", httpMethod="GET")
	@RequestMapping(value="/checkUrlPath",method = RequestMethod.GET)
	public RestResponse checkUrlPath(@RequestParam String UrlPath){
		return this.erpSysUrlService.checkUrlPath(UrlPath);
	}
	
	
	
}
