package com.nantian.erp.authentication.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import com.nantian.erp.authentication.service.ErpUrlPrivilegeService;
/** 
*
* Description: 服务启动时，将url同步到Redis中(Controller)
*
* @author gaoxiaodong
* @version 1.0
* <pre>
* Modification History: 
* Date                  Author           Version     
* ------------------------------------------------
* 2018年10月09日      		gaoxiaodong       1.0        
*        
* </pre>
*/
@Controller
public class ErpUrlPrivilegeController implements CommandLineRunner {

	@Autowired
	private ErpUrlPrivilegeService erpUrlPrivilegeService;

	/**
	 * Description：同步redis
	 * 
	 * @author 高晓冬
	 * @version 1.0
	 * @create 2018年10月9日
	 */
	@Override
	public void run(String... args) throws Exception {
		erpUrlPrivilegeService.SaveUrlForRedis();
			
	}

}
