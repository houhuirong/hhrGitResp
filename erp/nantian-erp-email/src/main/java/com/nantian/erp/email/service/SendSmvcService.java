package com.nantian.erp.email.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 短信验证码(Short Message Verification Code)发送service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年02月21日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
public class SendSmvcService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Description: 发送多附件的邮件
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月21日 上午10:08:29
	 */
	@Transactional
	public RestResponse sendSmvc(Map<String, Object> smvcParams) {
		logger.info("sendSmvc方法开始执行，参数是：smvcParams="+smvcParams);
		try {
			String checkCode = String.valueOf(smvcParams.get("checkCode"));//随机码
			int appid = Integer.valueOf(String.valueOf(smvcParams.get("appid")));//短信应用SDK AppID
			String appkey = String.valueOf(smvcParams.get("appkey"));//短信应用SDK AppKey
			String phone = String.valueOf(smvcParams.get("phone"));//接收短信的手机号码
			int templateId = Integer.valueOf(String.valueOf(smvcParams.get("templateId")));//短信模板ID，需要在短信应用中申请
			String smsSign = String.valueOf(smvcParams.get("smsSign"));//签名
			
			//数组具体的元素个数和模板中变量个数必须一致，例如事例中templateId:5678对应一个变量，参数数组中元素个数也必须是一个
			String[] params = {checkCode};
			SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
			// 签名参数未提供或者为空时，会使用默认签名发送短信
			SmsSingleSenderResult result = ssender.sendWithParam("86", phone, templateId, params, smsSign, "", "");
			logger.info("发送结果：result=" + result);
			return RestUtils.returnSuccessWithString("短信验证码发送成功！");
		} catch (Exception e) {
			logger.error("sendSmvc方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
}
