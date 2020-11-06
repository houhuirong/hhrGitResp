package com.nantian.erp.salary.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.dao.ErpTalkSalaryMapper;
import com.nantian.erp.salary.data.dao.ErpTalkSalaryRecordMapper;
import com.nantian.erp.salary.data.model.ErpTalkSalary;
import com.nantian.erp.salary.data.model.ErpTalkSalaryRecord;
import com.nantian.erp.salary.data.vo.ParamTalkSalaryVo;
import com.nantian.erp.salary.util.AesUtils;

/**
 * 招聘 -所有待入职 service 层
 * @author caoxb
 * @date 2018-09-06
 */
@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpToBeHiredService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${protocol.type}")
    private String protocolType;//http或https
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	private ErpTalkSalaryMapper erpTalkSalaryMapper;
	
	@Autowired
	private ErpTalkSalaryRecordMapper erpTalkSalaryRecordMapper;
	
	@SuppressWarnings({ "rawtypes" })
	@Autowired
	RedisTemplate redisTemplate;
	
	/**
	 * Description: 招聘-查询所有待入职
	 * @param  request       
	 * @param  response       
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月10日 下午13:32:53
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findAllToBeHired(String token) {
		logger.info("进入 招聘-查询所有待入职方法  无参数 ");
		List<Map<String, Object>> returnList = null;
		Map<String, Object> param = null;
		Map<String,Object> map=null;
		try {
			returnList = new ArrayList<Map<String,Object>>();			
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
			//调用ERP-人力资源 工程 的操作层服务接口-获取所有待入职员工及部门基本信息
			//String url = protocolType+"nantian-erp-hr/nantian-erp/erp/entry/findall?token="+token;
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/offer/findAllOffer?token="+token;
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(null,requestHeaders);
	        ResponseEntity<String> response= restTemplate.exchange(url,HttpMethod.GET,requestEntity,String.class);
	        String strResult=response.getBody();
	        JSONObject jsStr=JSON.parseObject(strResult);
	    	if(null == jsStr.get("data") || "".equals(String.valueOf(jsStr.get("data")))) {
	    		logger.info("访问hr工程allWaitingEntry接口,返回msg:"+jsStr.get("msg"));
				return RestUtils.returnFailure("调用hr工程没有查到待我处理的数据！");
			}
	    	Map<String,Object> allEntry=jsStr.getJSONObject("data");
	        if(allEntry!=null){
	        	List<Map<String,Object>> list=(List<Map<String, Object>>) allEntry.get("allWaitEntry");
	        	if(list==null){ 
	        		return RestUtils.returnFailure("无满足查询条件的数据返回！");
	        	}
				for (int i=0;i<list.size();i++){
					param=new HashMap<String, Object>();
					Map<String,Object> mapParam=list.get(i);
					//待入职人员简历ID
					Integer resumeId = Integer.valueOf(String.valueOf(null == mapParam.get("resumeId")?"0":mapParam.get("resumeId")));
					Integer offerId = Integer.valueOf(String.valueOf(null == mapParam.get("offerId")?"0":mapParam.get("offerId")));

					//根据简历ID查询员工的面试记录
					List<Object> resultList = this.findRecordByResumeId(resumeId,token);
					//面试谈薪
					ErpTalkSalary erpTalkSalary = this.erpTalkSalaryMapper.findOneByOfferId(offerId);
					if(erpTalkSalary!=null){
						erpTalkSalary.setAccumulationFundBase(decryptDataRsa(erpTalkSalary.getAccumulationFundBase()));
						erpTalkSalary.setMonthIncome(decryptDataRsa(erpTalkSalary.getMonthIncome()));
						erpTalkSalary.setSocialSecurityBase(decryptDataRsa(erpTalkSalary.getSocialSecurityBase()));
						erpTalkSalary.setBaseWage(decryptDataRsa(erpTalkSalary.getBaseWage()));
						erpTalkSalary.setMonthAllowance(decryptDataRsa(erpTalkSalary.getMonthAllowance()));
					}					
					//岗位信息
					Map<String, Object> postInfo = this.findPostInfo(offerId,token);
					param.put("postInfo", postInfo);              //岗位信息
					param.put("record", resultList);			  //面试记录
					param.put("ErpTalkSalaryInfo", erpTalkSalary);//面试谈薪
					param.put("empInfo", mapParam);//职工信息
					returnList.add(param);
				}		      
	        }
		} catch (Exception e) {
			logger.error("查询 招聘-查询所有待入职 方法 findAllToBeHired 出现异常 ："+e.getMessage(),e);
		}
		return RestUtils.returnSuccess(returnList);
	}
	
	/**
	 * Description: 招聘-根据简历ID查询面试记录
	 * @param  request       
	 * @param  response       
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月10日 下午13:32:53
	 */
	@SuppressWarnings("unchecked")
	private List<Object> findRecordByResumeId(Integer resumeId,String token) {
		logger.info("查询所有一级部门 参数 : " + resumeId);
		Map<String, Object> returnMap = null;
		List<Object> list = null;
		try {
			//调用ERP-人力资源 工程 的操作层服务接口-获取面试记录信息
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/resume/findInterviewRecord?resumeId="+resumeId;
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(null,requestHeaders);
	        ResponseEntity<String> response= restTemplate.exchange(url,HttpMethod.GET,requestEntity,String.class);
	        String resStr=response.getBody();
	        JSONObject jsStr=JSON.parseObject(resStr);
	    	if(null == jsStr.get("data") || "".equals(String.valueOf(jsStr.get("data")))) {
	    		logger.info("访问hr工程findInterviewRecord接口,返回msg为:"+jsStr.get("msg"));
				return new ArrayList<Object>();
			}
	        list=jsStr.getJSONArray("data");	        
		} catch (Exception e) {
			logger.error("根据简历ID查询面试记录 方法 findRecordByResumeId 出现异常 ："+e.getMessage(),e);
		}
		return list;
	}
	
	/**
	 * Description: 招聘-根据简历ID查询岗位信息
	 * @param  request       
	 * @param  response       
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月10日 下午13:32:53
	 */
	@SuppressWarnings("unchecked")
	private Map<String,Object> findPostInfo(Integer offerId,String token) {
		logger.info("查询所有一级部门 参数 ，offerId: " + offerId);
		Map<String, Object> returnMap = null;
		try {
			//调用ERP-人力资源 工程 的操作层服务接口-获取所有待入职员工及部门基本信息
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/empDepartment/Info/findPostInfo?offerId="+offerId;
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);//封装token
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(null,requestHeaders);
	        ResponseEntity<String> response= restTemplate.exchange(url,HttpMethod.GET,requestEntity,String.class);
	        String resStr=response.getBody();
	        JSONObject jsStr=JSON.parseObject(resStr);
	    	if(null == jsStr.get("data") || "".equals(String.valueOf(jsStr.get("data")))) {
	    		logger.info("访问hr工程findPostInfo接口,无岗位信息返回");
				return returnMap;
			}
	        returnMap = (Map<String, Object>) jsStr.getJSONObject("data"); 
		} catch (Exception e) {
			logger.error("根据简历ID查询岗位信息 方法findPostInfo 出现异常"+e.getMessage(),e);
		}
		return returnMap;
	}
	
	/**
	 * Description: 招聘-修改面试谈薪
	 * @param  request       
	 * @param  response       
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月10日 下午13:32:53
	 */
	public RestResponse updateTalkSalary(ParamTalkSalaryVo paramTalkSalaryVo) {
		logger.info("进入 修改面试谈薪方法 参数 : 简历ID" + paramTalkSalaryVo.getErpTalkSalary().getOfferId());
		String str = null;
		try {
			//修改面试谈薪
			ErpTalkSalary erpTalkSalary = paramTalkSalaryVo.getErpTalkSalary();
			//this.erpTalkSalaryMapper.updateErpTalkSalary(erpTalkSalary);
			//新增面试谈薪记录
			ErpTalkSalaryRecord erpTalkSalaryRecord = paramTalkSalaryVo.getErpTalkSalaryRecord();
			erpTalkSalaryRecord.setUpdateTime(ExDateUtils.getCurrentDateTime());
			this.erpTalkSalaryRecordMapper.insertTalkSalaryRecord(erpTalkSalaryRecord);
			str = "修改成功";
		} catch (Exception e) {
			logger.error("招聘-修改面试谈薪 方法updateTalkSalary 出现异常： "+e.getMessage());
		}
		return RestUtils.returnSuccessWithString(str);
	}
	/**
	 * Description: 测试解密
	 *
	 * @return
	 * @Author HouHuiRong
	 * @Create Date: 2018年11月9日 下午15:31:01
	 */
	public String decryptDataRsa(String salary) {
		String result="";
		if(salary==null){
			return result;
		}else{
		result = String.valueOf(AesUtils.decrypt(salary));
		}
		return result;
	}
	
}
