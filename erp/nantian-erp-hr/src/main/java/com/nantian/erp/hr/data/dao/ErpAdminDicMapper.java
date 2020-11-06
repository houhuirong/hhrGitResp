package com.nantian.erp.hr.data.dao;

import java.util.List;

import com.nantian.erp.hr.data.model.ErpAdminDic;

/** 
 * Description: 字典表mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月13日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpAdminDicMapper {
	
	//新增字典表一条记录
	public void insertAdminDic(ErpAdminDic adminDic);
	
	//删除字典表一条记录
	public void deleteAdminDic(Integer dicId);
	
	//修改字典表一条记录
	public void updateAdminDic(ErpAdminDic adminDic);
	
	//查询所有字典表的类别
	public List<ErpAdminDic> selectAllType();
	
	//根据字典类别查询全部的字典标识、字典名称
	public List<ErpAdminDic> selectAdminDicByType(String dicType);
	
	//根据字典类别查询字典标识的最后一位
	public Integer selectLastCodeByType(String dicType);
	
	//字典表去重（参数是：字典名字dic_name、字典类型dic_type）
	public ErpAdminDic selectAdminDicByParams(ErpAdminDic adminDic);
	
}
