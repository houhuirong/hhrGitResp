package com.nantian.erp.authentication.data.dao;

import java.util.List;
import com.nantian.erp.authentication.data.model.ErpSysPrivilege;
import com.nantian.erp.authentication.data.model.ErpSysUrl;
/**
 *
 * Description: 服务启动时，将url同步到Redis中(Mapper)
 *
 * @author gaoxiaodong
 * @version 1.0
 * 
 * <pre>
* Modification History: 
* Date                  Author           Version     
* ------------------------------------------------
* 2018年10月09日      		gaoxiaodong       1.0
*  </pre>
 */
public interface ErpUrlPrivilegeMapper {

	public List<ErpSysPrivilege> urlIdByPrivilegeAccess();

	public ErpSysUrl getUrlData(Integer urlId);
	
}
