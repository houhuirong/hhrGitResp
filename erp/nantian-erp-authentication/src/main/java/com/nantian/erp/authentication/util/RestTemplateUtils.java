package com.nantian.erp.authentication.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.nantian.erp.common.base.util.HttpClientUtil;

/** 
 * @description: 跨工程调用工具方法
 * @author ZhangYuWei
 * @create 2019年04月02日
 */
@Component
@PropertySource(value= {"classpath:config/email.properties",
		"file:${spring.profiles.path}/config/email.properties",
		"classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties"},
		ignoreResourceNotFound = true)
public class RestTemplateUtils {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/*
	 * 从配置文件中获取请求协议相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	/*
	 * 从配置文件中获取Email相关属性
	 */
	@Value("${email.service.host}")
	private  String emailServiceHost;//邮件服务的IP地址和端口号
	@Value("${environment.type}")
	private  String environmentType;//环境类型（根据该标识，决定邮件的发送人、抄送人、收件人）
	@Value("${test.email.frommail}")
	private String testEmailFrommail;//测试环境发件人
	@Value("${test.email.bcc}")
	private String testEmailBcc;//测试环境抄送人
	@Value("${test.email.tomail}")
	private String testEmailTomail;//测试环境收件人
	
	/** 
	 * @description: 调用email工程，发送邮件
	 * @author ZhangYuWei
	 * @create 2019年04月02日
	 */
	@SuppressWarnings("unchecked")
	public Boolean sendEmail(String frommail,String bcc,String subject,String text,String tomail) {
    	logger.info("进入sendEmail方法，参数是：frommail={},bcc={},subject={},text={},tomail={}",
    			frommail,bcc,subject,text,tomail);
    	boolean sendSuccess = true;
        try {
        	if("test".equals(environmentType)) {
				frommail = testEmailFrommail;
				bcc = testEmailBcc;
				tomail = testEmailTomail;
				logger.info("测试环境......发件人：{},抄送人：{},收件人：{}",frommail,bcc,tomail);
			}
			logger.info("实际环境：{}",environmentType);
			
			//调用邮件管理工程，将发邮件送必要的参数传递过去，并发送无附件的邮件
			String emailUrl = emailServiceHost+"/nantian-erp/email/send/withoutAttachment";
			Map<String,String> emailParams = new HashMap<>();
			emailParams.put("frommail", frommail);
			emailParams.put("bcc", bcc);
			emailParams.put("subject", subject);
			emailParams.put("text", text);
			emailParams.put("tomail", tomail);
			Map<String,String> headers = null;
			Map<String, String> emailResponse = HttpClientUtil.executePostMethodWithParas(emailUrl, JSON.toJSONString(emailParams), headers, "application/json", 30000);
			String code = emailResponse.get("code");//响应码
			String result = emailResponse.get("result");//响应结果
			logger.info("code={},result={}",code,result);
			if(!"200".equals(code)) {
				logger.error("邮件工程响应失败！");
				sendSuccess = false;
			}
			Map<String,Object> emailResultMap = (Map<String,Object>) JSON.parse(result);
			String statusOfResult = (String) emailResultMap.get("status");//方法调用的返回码
			if(!"200".equals(statusOfResult)) {
				logger.error("邮件发送失败！");
				sendSuccess = false;
			}
        } catch (Exception e) {
            logger.error("sendEmail异常："+e.getMessage(),e);
            sendSuccess = false;
        }
        return sendSuccess;
    }
	
	/** 
	 * @description: 调用email工程，发送邮件（单个附件）
	 * @author ZhangYuWei
	 * @create 2019年04月02日
	 */
	@SuppressWarnings("unchecked")
	public Boolean sendEmailWithAttachment(String frommail,String bcc,String subject,
			String text,String tomail,String filename,String filepath) {
    	logger.info("进入sendEmailWithAttachment方法，参数是：frommail={},bcc={},subject={},"
    			+ "text={},tomail={},filename={},filepath={}",
    			frommail,bcc,subject,text,tomail,filename,filepath);
    	boolean sendSuccess = true;
        try {
        	if("test".equals(environmentType)) {
				frommail = testEmailFrommail;
				bcc = testEmailBcc;
				tomail = testEmailTomail;
				logger.info("测试环境......发件人：{},抄送人：{},收件人：{}",frommail,bcc,tomail);
			}
			logger.info("实际环境：{}",environmentType);
			
			//调用邮件管理工程，将发邮件送必要的参数传递过去，并发送无附件的邮件
			String emailUrl = emailServiceHost+"/nantian-erp/email/send/withAttachment";
			Map<String,String> emailParams = new HashMap<>();
			emailParams.put("frommail", frommail);
			emailParams.put("bcc", bcc);
			emailParams.put("subject", subject);
			emailParams.put("text", text);
			emailParams.put("tomail", tomail);
			emailParams.put("filename", filename);//附件名字
			emailParams.put("filepath", filepath);//附件路径
			Map<String,String> headers = null;
			Map<String, String> emailResponse = HttpClientUtil.executePostMethodWithParas(emailUrl, JSON.toJSONString(emailParams), headers, "application/json", 30000);
			String code = emailResponse.get("code");//响应码
			String result = emailResponse.get("result");//响应结果
			logger.info("code={},result={}",code,result);
			if(!"200".equals(code)) {
				logger.error("邮件工程响应失败！");
				sendSuccess = false;
			}
			Map<String,Object> emailResultMap = (Map<String,Object>) JSON.parse(result);
			String statusOfResult = (String) emailResultMap.get("status");//方法调用的返回码
			if(!"200".equals(statusOfResult)) {
				logger.error("邮件发送失败！");
				sendSuccess = false;
			}
        } catch (Exception e) {
            logger.error("sendEmail异常："+e.getMessage(),e);
            sendSuccess = false;
        }
        return sendSuccess;
    }
	
}
