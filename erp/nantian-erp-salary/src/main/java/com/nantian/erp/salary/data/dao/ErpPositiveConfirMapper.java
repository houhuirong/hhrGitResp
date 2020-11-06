package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpPositiveConfirm;

/**
 * @author gaolp
 * @date 2019年10月14日
 * <p>description: 转正确认名单操作接口</p>
 */
public interface ErpPositiveConfirMapper {
	
	//条件查询
	public List<Map<String,Object>> seleConfirmByparam(Map<String,Object> parmas);
	//根据年月查询
	public Map<String,Object> seleConfirmByear(String yearMonth);
	//新增
	public void  insertConfirm(ErpPositiveConfirm erpConfirm);
	//修改
	public void  updateConfirm(ErpPositiveConfirm erpConfirm);
}
