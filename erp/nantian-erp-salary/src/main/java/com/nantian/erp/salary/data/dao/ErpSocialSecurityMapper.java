package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpSocialSecurity;

/** 
 * Description: 社保表mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月20日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpSocialSecurityMapper {
	
	//新增社保表一条记录
	void insertSocialSecurity(ErpSocialSecurity socialSecurity);
	
	//修改社保表一条记录
	void updateSocialSecurity(ErpSocialSecurity socialSecurity);
	
	//查询最新的一条社保表记录
	ErpSocialSecurity selectSocialSecurityLastOne();
	
	//查询全部的社保表记录
	List<ErpSocialSecurity> selectSocialSecurityAll();
	//根据时间查询社保表记录
	ErpSocialSecurity selectSocialSecurityByTime(String queryTime);
	
}
