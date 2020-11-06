package com.nantian.erp.salary.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.dao.ErpSalaryRangeSetMapper;
import com.nantian.erp.salary.data.model.ErpSalaryRangeSet;
import com.nantian.erp.salary.util.AesUtils;

@Service
public class ErpSalaryRangeSetService {
	private final Logger logger=LoggerFactory.getLogger(this.getClass());
	
	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ErpSalaryRangeSetMapper erpSalaryRangeSetMapper;

	public RestResponse findSalaryRangeSetByRank(Integer erpPositionNo) {
		logger.info("findSalaryRangeSetByRank方法开始执行,参数erpPositionNo:"+erpPositionNo);
		ErpSalaryRangeSet erpSalaryRangeSet=null;
		try{
			erpSalaryRangeSet=this.erpSalaryRangeSetMapper.findSalaryRangeSetByRank(erpPositionNo);
			if(null==erpSalaryRangeSet){
				logger.info("findSalaryRangeSetByRankf方法通过职位职级编号erpPositionNo:"+erpPositionNo+"未设置该岗位职级的社保范围!");
				return RestUtils.returnSuccess("未设置该岗位职级的社保范围!");
			}
			erpSalaryRangeSet.setErpSalaryMax(AesUtils.decrypt(erpSalaryRangeSet.getErpSalaryMax()));
			erpSalaryRangeSet.setErpSalaryMin(AesUtils.decrypt(erpSalaryRangeSet.getErpSalaryMin()));
			erpSalaryRangeSet.setErpSocialSecurityMax(AesUtils.decrypt(erpSalaryRangeSet.getErpSocialSecurityMax()));
			erpSalaryRangeSet.setErpSocialSecurityMin(AesUtils.decrypt(erpSalaryRangeSet.getErpSocialSecurityMin()));
		}catch(Exception e){
			logger.error("findSalaryRangeSetByRank方法出现异常:"+e.getMessage(),e);
		}
		return RestUtils.returnSuccess(erpSalaryRangeSet);
	}
	
//	public RestResponse findAllSalaryRangeSet() {
//		logger.info("findAllSalaryRangeSet方法开始执行,无参数");
//		List<ErpSalaryRangeSet> returnList=new ArrayList<ErpSalaryRangeSet>();
//		try{
//			List<ErpSalaryRangeSet> list=this.erpSalaryRangeSetMapper.findAllSalaryRangeSet();
//			if(null==list||list.size()==0){
//				logger.info("findAllSalaryRangeSet方法查询所有职级薪资范围设置返回结果为空!");
//				return RestUtils.returnSuccess("findAllSalaryRangeSet方法查询所有职级薪资范围设置返回结果为空!");
//			}
//			for(ErpSalaryRangeSet erpSalaryRangeSet:list){
//				erpSalaryRangeSet.setErpSalaryMax(AesUtils.decrypt(erpSalaryRangeSet.getErpSalaryMax()));
//				erpSalaryRangeSet.setErpSalaryMin(AesUtils.decrypt(erpSalaryRangeSet.getErpSalaryMin()));
//				erpSalaryRangeSet.setErpSocialSecurityMax(AesUtils.decrypt(erpSalaryRangeSet.getErpSocialSecurityMax()));
//				erpSalaryRangeSet.setErpSocialSecurityMin(AesUtils.decrypt(erpSalaryRangeSet.getErpSocialSecurityMin()));
//				returnList.add(erpSalaryRangeSet);
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//			logger.error("findAllSalaryRangeSet方法出现异常:"+e.getMessage(),e);
//		}
//		return RestUtils.returnSuccess(returnList);
//	}
	
	/**
	 * Description: 职位职级的薪资范围设置
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月18日 下午14:42:48
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findAllSalaryRangeSet(Map<String,Object> params,String token) {
		logger.info("findAllSalaryRangeSet方法开始执行，参数是：params="+params);
		try{
			/*
			 * 调用人力资源工程查询用户的员工姓名
			 */
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/postTemplate/findPositionRankList";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);//将token放到请求头中
			HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(params, requestHeaders);
			
			ResponseEntity<RestResponse> response = this.restTemplate.postForEntity(url, requestEntity, RestResponse.class);
			if(200 != response.getStatusCodeValue() || !"200".equals(response.getBody().getStatus())) {
				return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
			}
			
			List<Map<String,Object>> positionRankList = (List<Map<String, Object>>) response.getBody().getData();
			
			for (Map<String, Object> positionRankMap : positionRankList) {
				//将HR工程返回的字段赋值给新字段，并把旧字段删除
				positionRankMap.put("erpPositionNo", positionRankMap.get("positionNo"));
				positionRankMap.remove("positionNo");
				Integer positionNo = Integer.valueOf(String.valueOf(positionRankMap.get("erpPositionNo")));
				ErpSalaryRangeSet erpSalaryRangeSet = erpSalaryRangeSetMapper.findSalaryRangeSetByRank(positionNo);
				if(erpSalaryRangeSet!=null) {
					positionRankMap.put("erpSalaryRangeSetId", erpSalaryRangeSet.getErpSalaryRangeSetId());
					positionRankMap.put("erpSalaryMax", AesUtils.decrypt(erpSalaryRangeSet.getErpSalaryMax()));
					positionRankMap.put("erpSalaryMin", AesUtils.decrypt(erpSalaryRangeSet.getErpSalaryMin()));
					positionRankMap.put("erpSocialSecurityMax", AesUtils.decrypt(erpSalaryRangeSet.getErpSocialSecurityMax()));
					positionRankMap.put("erpSocialSecurityMin", AesUtils.decrypt(erpSalaryRangeSet.getErpSocialSecurityMin()));
				}
			}
			logger.info("positionRankList="+positionRankList);		
			return RestUtils.returnSuccess(positionRankList);
		}catch(Exception e){
			logger.error("findAllSalaryRangeSet方法出现异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
		
	}

	@Transactional
	public RestResponse insertSalaryRangeSet(ErpSalaryRangeSet erpSalaryRangeSet) {
		logger.info("insertSalaryRangeSet方法开始执行，传递参数："+erpSalaryRangeSet);
		try {
			if(null==erpSalaryRangeSet){
				logger.info("insertSalaryRangeSet参数erpSalaryRangeSet:"+erpSalaryRangeSet);
				return RestUtils.returnSuccess("erpSalaryRangeSet对象为null,新增失败!");
			}
			ErpSalaryRangeSet salaryRangeSet=this.erpSalaryRangeSetMapper.findSalaryRangeSetByRank(erpSalaryRangeSet.getErpPositionNo());
			if(salaryRangeSet!=null){
				return RestUtils.returnSuccess("已存在该职级的薪资范围设置！");
			}
			erpSalaryRangeSet.setErpPositionNo(erpSalaryRangeSet.getErpPositionNo());
			erpSalaryRangeSet.setErpSalaryMax(AesUtils.encrypt(erpSalaryRangeSet.getErpSalaryMax()));
			erpSalaryRangeSet.setErpSalaryMin(AesUtils.encrypt(erpSalaryRangeSet.getErpSalaryMin()));
			erpSalaryRangeSet.setErpSocialSecurityMax(AesUtils.encrypt(erpSalaryRangeSet.getErpSocialSecurityMax()));
			erpSalaryRangeSet.setErpSocialSecurityMin(AesUtils.encrypt(erpSalaryRangeSet.getErpSocialSecurityMin()));
			erpSalaryRangeSetMapper.insertSalaryRangeSet(erpSalaryRangeSet);
			return RestUtils.returnSuccess("新增成功!");
		} catch (Exception e) {
			logger.error("insertSalaryRangeSet方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("insertSalaryRangeSet方法出现异常！新增失败！");
		}
	}
	
	/**
	 * Description: 职位职级的薪资范围设置
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月19日 上午09:35:32
	 */
	@Transactional
	public RestResponse updateSalaryRangeSet(ErpSalaryRangeSet erpSalaryRangeSet) {
		logger.info("updateSalaryRangeSet方法开始执行，传递参数："+erpSalaryRangeSet);
		try {
			if(null==erpSalaryRangeSet){
				logger.info("updateSalaryRangeSet参数erpSalaryRangeSet:"+erpSalaryRangeSet);
				return RestUtils.returnSuccess("erpSalaryRangeSet对象为null,修改失败!");
			}
			erpSalaryRangeSet.setErpSalaryMax(AesUtils.encrypt(erpSalaryRangeSet.getErpSalaryMax()));
			erpSalaryRangeSet.setErpSalaryMin(AesUtils.encrypt(erpSalaryRangeSet.getErpSalaryMin()));
			erpSalaryRangeSet.setErpSocialSecurityMax(AesUtils.encrypt(erpSalaryRangeSet.getErpSocialSecurityMax()));
			erpSalaryRangeSet.setErpSocialSecurityMin(AesUtils.encrypt(erpSalaryRangeSet.getErpSocialSecurityMin()));
			
			/*
			 * 查询数据库有没有该职级的薪资范围？如果没有，则新增；如果有，则更新。
			 */
			ErpSalaryRangeSet salaryRangeSet = this.erpSalaryRangeSetMapper.findSalaryRangeSetByRank(erpSalaryRangeSet.getErpPositionNo());
			if(salaryRangeSet == null){
				erpSalaryRangeSetMapper.insertSalaryRangeSet(erpSalaryRangeSet);
			}else {
				erpSalaryRangeSet.setErpSalaryRangeSetId(salaryRangeSet.getErpSalaryRangeSetId());
				System.out.println("erpSalaryRangeSet" + erpSalaryRangeSet);
				erpSalaryRangeSetMapper.updateSalaryRangeSet(erpSalaryRangeSet);
			}
			return RestUtils.returnSuccess("修改成功!");
		} catch (Exception e) {
			logger.error("updateSalaryRangeSet方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("updateSalaryRangeSet方法出现异常！修改失败！");
		}
	}
	
	@Transactional
	public RestResponse deleteSalaryRangeSet(Integer erpSalaryRangeSetId) {
		logger.info("deleteSalaryRangeSet方法开始执行，传递参数rank："+erpSalaryRangeSetId);
		try {
			if(erpSalaryRangeSetId == null){
				return RestUtils.returnSuccess("该职位还没有设置薪酬范围，不能删除！");
			}
			this.erpSalaryRangeSetMapper.deleteSalaryRangeSet(erpSalaryRangeSetId);
			return RestUtils.returnSuccess("删除成功！");
		} catch (Exception e) {
			logger.error("deleteSalaryRangeSet方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("deleteSalaryRangeSet方法出现异常！删除失败！");
		}
	}

}
