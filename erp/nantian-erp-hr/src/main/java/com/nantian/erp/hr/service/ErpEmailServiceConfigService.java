package com.nantian.erp.hr.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.dao.TEmailServiceConfigMapper;
import com.nantian.erp.hr.data.model.ErpDimission;
import com.nantian.erp.hr.data.model.TEmailServiceConfig;

/** 
 * Description: EmailServiceConfig的Service
 *
 * @author hhr
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2020年05月26日      		hhr          1.0       
 * </pre>
 */
@Service
public class ErpEmailServiceConfigService {
	@Autowired
	private TEmailServiceConfigMapper emailConfigMapper;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 查询所有邮件配置
	 * @return RestResponse
	 */
	public RestResponse findEmailConfigDetail(Integer send) {
		logger.info("findEmailConfigDetail方法开始执行，传递参数");
	    List<Map<String,Object>> tempList=new ArrayList<Map<String,Object>>();
		try {
			Map<String,Object> map=new HashMap<>();
			map.put("send", send);
			tempList=this.emailConfigMapper.selectByParam(map);  //所有邮件配置
		} catch (Exception e) {
			logger.info("findEmailConfigDetail方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess(tempList);
	}
	/**
	 * 修改邮件配置
	 * @return RestResponse
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse updateEmailConfig(TEmailServiceConfig emailServiceConfig) {
		logger.info("updateEmailConfig方法开始执行,参数={}",emailServiceConfig.toString());
		 this.emailConfigMapper.updateByPrimaryKeySelective(emailServiceConfig);
		 return RestUtils.returnSuccess("修改成功！");
	}

}
