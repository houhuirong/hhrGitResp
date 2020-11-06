package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.ErpResume;

/** 
 * Description: 简历mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月05日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpResumeMapper {
	
	//查询 所有的简历  根据参数Valid 判断结果是 有效的或是失效的
	public List<Map<String, Object>> findResumeByValid(Map<String, Object> param);
	
	//插入简历信息
	public void insertResume(ErpResume resume);
	
	//修改简历信息
	public void updateResume(ErpResume resume);
	
	//根据简历ID查询简历信息
	public Map<String, Object> selectResumeDetail(Integer resumeId);
	
	//查询全部有效简历
	public List<Map<String, Object>> findValidResume(Map<String, Object> params);
	
	//分页+模糊条件查询失效简历
	public List<Map<String, Object>> findArchivedResume(Map<String, Object> params);
	
	//查询失效简历总数
	public Long findTotalCountOfArchivedResume(Map<String, Object> params);
	
	//校验手机号码、邮箱是否重复
	public ErpResume validPhoneAndEmail(Map<String,Object> params);
	
}
