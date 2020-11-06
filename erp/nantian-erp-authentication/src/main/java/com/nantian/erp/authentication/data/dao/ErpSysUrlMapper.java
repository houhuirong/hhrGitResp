package com.nantian.erp.authentication.data.dao;

import java.util.List;
import com.nantian.erp.authentication.data.vo.ErpSysModulVo;
import com.nantian.erp.authentication.data.vo.ErpSysUrlVo;

/** 
 * Description: 模块-url-管理- mapper接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0        
 * </pre>
 */
public interface ErpSysUrlMapper {
	
	//查询所有模块
	public List<ErpSysModulVo> findAllModul();
	
	//根据模块ID查询下面的url集合
	public List<ErpSysUrlVo> findAllUrlToModulId(Integer modulId);
	
	//新增url信息
	public void insertUrl(ErpSysUrlVo erpSysUrlVo);
	
	//修改url信息
	public void updateUrl(ErpSysUrlVo erpSysUrlVo);
	
	//删除url信息
	public void deleteUrl(Integer urlId);
	
	//url路径验证
	public ErpSysUrlVo checkUrlPath(String UrlPath);	

	//删除url信息
	public ErpSysUrlVo getUrlInfoById(Integer urlId);
	

}
