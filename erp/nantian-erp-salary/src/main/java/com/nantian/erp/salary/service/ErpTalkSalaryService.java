package com.nantian.erp.salary.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.util.AesUtils;
import com.nantian.erp.salary.data.dao.ErpTalkSalaryMapper;
import com.nantian.erp.salary.data.model.ErpTalkSalary;

/** 
 * Description: 面试谈薪service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月14日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpTalkSalaryService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${protocol.type}")
    private String protocolType;//http或https
	@Autowired 
	private ErpTalkSalaryMapper erpTalkSalaryMapper;
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Description: 面试谈薪-新增结果到员工薪酬系统
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月14日 下午13:36:40
	 */
	@Transactional
	public RestResponse insertErpTalkSalary(ErpTalkSalary talkSalary) {
		logger.info("进入insertErpTalkSalary方法，参数是："+talkSalary);
		try {
			
			ErpTalkSalary erpTalkSalaryTemp = this.erpTalkSalaryMapper.findOneByOfferId(talkSalary.getOfferId());
			if(erpTalkSalaryTemp==null) {
				erpTalkSalaryMapper.insertTalkSalary(talkSalary);
			}else {
				erpTalkSalaryMapper.updateTalkSalary(talkSalary);
			}
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("insertErpTalkSalary出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致谈薪数据新增失败 ！");
		}
	}
	/**
	 * Description: 面试谈薪-修改面试谈薪信息
	 *
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年2月14日 下午13:36:40
	 */
	@Transactional
	public RestResponse updateErpTalkSalary(ErpTalkSalary talkSalary,String token) {
		logger.info("进入updateErpTalkSalary方法，参数是："+talkSalary);
		try {
			/*update by gaolp
			 * date 2019-08-20
			 * 更新时查询谈薪表是否有数据，没有则新增
			 */
			ErpTalkSalary erpTalkSalaryTemp = erpTalkSalaryMapper.findOneByOfferId(talkSalary.getOfferId());
			if (erpTalkSalaryTemp == null) {
				//薪酬数据加密
				if(talkSalary.getMonthIncome()!=null) {
					talkSalary.setMonthIncome(AesUtils.encrypt(talkSalary.getMonthIncome()));
				}
				if(talkSalary.getSocialSecurityBase()!=null) {
					talkSalary.setSocialSecurityBase(AesUtils.encrypt(talkSalary.getSocialSecurityBase()));
				}
				if(talkSalary.getAccumulationFundBase()!=null) {
					talkSalary.setAccumulationFundBase(AesUtils.encrypt(talkSalary.getAccumulationFundBase()));
				}
				if(talkSalary.getBaseWage()!=null) {
					talkSalary.setBaseWage(AesUtils.encrypt(talkSalary.getBaseWage()));
				}
				if(talkSalary.getMonthAllowance()!=null) {
					talkSalary.setMonthAllowance(AesUtils.encrypt(talkSalary.getMonthAllowance()));
				}
				erpTalkSalaryMapper.insertTalkSalary(talkSalary);
			}else{
				//薪酬数据加密
				if(talkSalary.getMonthIncome()!=null) {
					talkSalary.setMonthIncome(AesUtils.encrypt(talkSalary.getMonthIncome()));
				}
				if(talkSalary.getSocialSecurityBase()!=null) {
					talkSalary.setSocialSecurityBase(AesUtils.encrypt(talkSalary.getSocialSecurityBase()));
				}
				if(talkSalary.getAccumulationFundBase()!=null) {
					talkSalary.setAccumulationFundBase(AesUtils.encrypt(talkSalary.getAccumulationFundBase()));
				}
				if(talkSalary.getBaseWage()!=null) {
					talkSalary.setBaseWage(AesUtils.encrypt(talkSalary.getBaseWage()));
				}
				if(talkSalary.getMonthAllowance()!=null) {
					talkSalary.setMonthAllowance(AesUtils.encrypt(talkSalary.getMonthAllowance()));
				}
				
				erpTalkSalaryMapper.updateTalkSalary(talkSalary);
				
				//跨工程调用，像hr工程写入修改记录
				String url = protocolType+"nantian-erp-hr/nantian-erp/erp/offer/talkSalaryChange";
				Map<String,Object> erpTalkSalary = new HashMap<>();
				erpTalkSalary.put("offerId", talkSalary.getOfferId());
				HttpHeaders requestHeaders=new HttpHeaders();
				requestHeaders.add("token",token.toString());//将token放到请求头中
				HttpEntity<Map<String,Object>> request = new HttpEntity<>(erpTalkSalary, requestHeaders);
				
				ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
				if(response.getStatusCodeValue() != 200){
					logger.error("hr工程响应失败！导致薪酬修改记录插入失败！");
				}
			}
			return RestUtils.returnSuccessWithString("修改成功！");
		} catch (Exception e) {
			logger.error("insertErpTalkSalary出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致谈薪数据新增失败 ！");
		}
	}
	
	/**
	 * Description: 面试谈薪-根据offerId查询员工薪酬数据
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月20日 下午10:43:08
	 */
	public RestResponse findErpTalkSalary(Integer offerId) {
		logger.info("进入findErpTalkSalary方法，参数是：offerId="+offerId);
		try {
			ErpTalkSalary erpTalkSalaryTemp = this.erpTalkSalaryMapper.findOneByOfferId(offerId);
			return RestUtils.returnSuccess(erpTalkSalaryTemp);
		} catch (Exception e) {
			logger.error("findErpTalkSalary出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致谈薪数据查询失败 ！");
		}
	}
	
}
