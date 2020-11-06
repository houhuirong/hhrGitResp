package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.ErpOffer;

/** 
 * Description: offer表mapper
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
public interface ErpOfferMapper{
	
	//插入Offer信息
	void insertOffer(ErpOffer offer);
	
	//查询岗位发布中的有效的offer
	public Integer selectCountAllValiOffer(Integer postId);
	
	//修改Offer信息
	void updateOffer(ErpOffer offer);
	
	//根据offerId查询offer信息
	Map<String, Object> selectOfferDetail(Integer offerId);
	
	//条件查询全部offer信息
	List<Map<String, Object>> selectOfferInfoByParams(Map<String,Object> params);
	
	//查询全部归档offer
	List<Map<String, Object>> selsectAllInvalidOffer(Map<String,Object> params);
	
	//条件查询全部offer信息
	List<ErpOffer> selectOfferAutomaticInvalid();
	
	//查询一个岗位的已发offer人数
	Integer selectCountAllOffer(Integer postId);

	/**
	 * 根据面试id列表查询该简历的offer记录id列表
	 * @param resumePostIds 面试id列表
	 * @return
	 */
	List<Integer> findIdsByResumePostIds(@Param("resumePostIds") List<Integer> resumePostIds)throws Exception;

	/**
	 * 根据offerid列表修改offer为失效
	 * @param offerIds 
	 */
	void updateValidFalseByOfferIds(@Param("offerIds") List<Integer> offerIds)throws Exception;

    List<Map<String, Object>> findAllOffer(Map<String, Object> queryMap);
}
