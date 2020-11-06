package com.nantian.erp.hr.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.dao.ErrorEmailLogMapper;
import com.nantian.erp.hr.data.model.ErrorEmailLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ErrorEmailLogMapper errorEmailLogMapper;
	/** 
	 * @description: 调用authentication工程，通过员工ID查询用户的邮箱（username字段）
	 * 定时任务调用时，token可以为null
	 * @author ZhangYuWei
	 * @create 2019年04月02日
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String findUsernameByEmployeeId(String token,Integer employeeId) {
    	logger.info("进入findUsernameByEmployeeId方法，参数是：token={},employeeId={}",token,employeeId);
        try {
        	String url = "";
        	if(token==null) {
        		url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpIdScheduler?empId="+employeeId;
        	}else {
        		url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpId?empId="+employeeId;
        	}
			
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
		    HttpEntity<String> requestEntity=new HttpEntity<>(null,requestHeaders);
		    ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.GET,requestEntity,Map.class);
		    logger.info("跨工程调用的响应结果response={}",response);
		    Map<String,Object> resultMap = response.getBody();
		    if(response.getStatusCodeValue() != 200 || resultMap.get("data")==null || "".equals(resultMap.get("data"))){
		    	logger.error("权限工程响应失败！");
		    	return null;
		    }
		    Map<String,Object> userMap = (Map<String, Object>) resultMap.get("data");
		    if(userMap==null || userMap.get("username")==null){
		    	logger.error("通过用户ID未获取到用户信息");
		    	return null;
		    }
		    return String.valueOf(userMap.get("username"));//获取用户邮箱
        } catch (Exception e) {
            logger.error("findUsernameByEmployeeId："+e.getMessage(),e);
            return null;
        }
    }
	
	/** 
	 * @description: 调用authentication工程，通过角色ID查询员工列表
	 * @author ZhangYuWei
	 * @create 2019年06月04日
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String,Object>> findAllUserByRoleId(String token,Integer roleId) {
    	logger.info("进入findAllUserByRoleId方法，参数是：token={},roleId={}",token,roleId);
        try {
        	String url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findAllUserByRoleId?roleId="+roleId;
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
		    HttpEntity<String> requestEntity=new HttpEntity<>(null,requestHeaders);
		    ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.GET,requestEntity,Map.class);
		    logger.info("跨工程调用的响应结果response={}",response);
		    
		    Map<String,Object> resultMap = response.getBody();
		    if(response.getStatusCodeValue() != 200 || resultMap.get("data")==null || "".equals(resultMap.get("data"))){
		    	logger.error("权限工程响应失败！");
		    	return null;
		    }
		    List<Map<String,Object>> userList = (List<Map<String,Object>>) resultMap.get("data");
		    if(userList==null || userList.isEmpty()){
		    	logger.error("通过角色ID未获取到用户列表");
		    	return null;
		    }
		    return userList;//该角色的用户列表
        } catch (Exception e) {
            logger.error("findAllUserByRoleId："+e.getMessage(),e);
            return null;
        }
    }
	
	/** 
	 * @description: 调用email工程，发送邮件
	 * @author ZhangYuWei
	 * @create 2019年04月02日
	 */
	@SuppressWarnings("unchecked")
	public Boolean sendEmail(String frommail,String bcc,String subject,String text,String tomail,Integer emailServiceType,Integer id){
    	logger.info("进入sendEmail方法，参数是：frommail={},bcc={},subject={},tomail={}",
    			frommail,bcc,subject,tomail);
    	boolean sendSuccess = true;
    	ErrorEmailLog errorEmailLog = new ErrorEmailLog();//封装邮件日志
    	String errorLog="";//日志内容
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
			//发件人
			errorEmailLog.setSender(frommail);
			//收件人
			errorEmailLog.setRecipient(tomail);
			//抄送人
			errorEmailLog.setBcc(bcc);
			//邮件主题
			errorEmailLog.setSubject(subject);
			//邮件内容
			errorEmailLog.setEmailMessage(text);
			//邮件服务类型
			errorEmailLog.setEmailServiceType(emailServiceType);
			if(!"200".equals(code)) {
				logger.error("邮件工程响应失败！");
				sendSuccess = false;
				errorEmailLog.setType(0);//发送失败
				errorEmailLog.setErrorLog("邮件工程响应失败！");// 错误日志
				return sendSuccess;
			}
			Map<String,Object> emailResultMap = (Map<String,Object>) JSON.parse(result);
			String statusOfResult = String.valueOf(emailResultMap.get("status"));//方法调用的返回码
			errorLog = String.valueOf(emailResultMap.get("data"));//方法调用的返回值
			if(!"200".equals(statusOfResult)) {
				logger.error("邮件发送失败！");
				sendSuccess = false;
				errorEmailLog.setType(0);//发送失败
				errorEmailLog.setErrorLog(errorLog);// 错误日志
				return sendSuccess;
			}
			errorEmailLog.setType(1);//发送成功
			errorEmailLog.setErrorLog(errorLog);
			errorEmailLog.setSendTime(new Date());
        } catch (Exception e) {
            logger.error("sendEmail异常："+e.getMessage(),e);
            sendSuccess = false;
        }finally{
        	saveEmailLog(id,sendSuccess,errorEmailLog);
        }
        return sendSuccess;
    }
	
	/** 
	 * @description: 调用email工程，发送邮件（单个附件）
	 * @author ZhangYuWei
	 * @throws Exception 
	 * @create 2019年04月02日
	 */
	@SuppressWarnings("unchecked")
	public Boolean sendEmailWithAttachment(String frommail,String bcc,String subject,
			String text,String tomail,String filename,String filepath,Integer emailServiceType,Integer id){
    	logger.info("进入sendEmailWithAttachment方法，参数是：frommail={},bcc={},subject={},"
    			+ "tomail={},filename={},filepath={}",
    			frommail,bcc,subject,tomail,filename,filepath);
    	boolean sendSuccess = true;
    	ErrorEmailLog errorEmailLog = new ErrorEmailLog();//封装邮件日志
    	String errorLog="";//错误日志
        try {
        	if("test".equals(environmentType)) {
				frommail = testEmailFrommail;
				bcc = testEmailBcc;
				tomail = testEmailTomail;
				logger.info("测试环境......发件人：{},抄送人：{},收件人：{}",frommail,bcc,tomail);
			}
			logger.info("实际环境：{}",environmentType);
			
			//调用邮件管理工程，将发邮件送必要的参数传递过去，并发送单附件的邮件
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
			//发件人
			errorEmailLog.setSender(frommail);
			// 收件人
			errorEmailLog.setRecipient(tomail);
			//抄送人
			errorEmailLog.setBcc(bcc);
			//邮件主题
			errorEmailLog.setSubject(subject);
			// 邮件内容
			errorEmailLog.setEmailMessage(text);
			//附件路径
			errorEmailLog.setAttachmentPath(filepath);
			//邮件服务类型
			errorEmailLog.setEmailServiceType(emailServiceType);
			if(!"200".equals(code)) {
				logger.error("邮件工程响应失败！");
				sendSuccess = false;
				errorEmailLog.setType(0);//发送失败
				errorEmailLog.setErrorLog("邮件工程响应失败！");// 错误日志
				return sendSuccess;
			}
			Map<String,Object> emailResultMap = (Map<String,Object>) JSON.parse(result);
			String statusOfResult = String.valueOf(emailResultMap.get("status"));//方法调用的返回码
			errorLog = String.valueOf(emailResultMap.get("data"));//方法调用的返回值

			if(!"200".equals(statusOfResult)) {
				logger.error("邮件发送失败！");
				sendSuccess = false;
				errorEmailLog.setType(0);//发送失败
				errorEmailLog.setErrorLog(errorLog);// 错误日志
				return sendSuccess;
			}
			errorEmailLog.setType(1);//发送成功
			errorEmailLog.setErrorLog(errorLog);
			errorEmailLog.setSendTime(new Date());
        } catch (Exception e) {
            logger.error("sendEmail异常："+e.getMessage(),e);
            sendSuccess = false;
        }finally{
        	saveEmailLog(id,sendSuccess,errorEmailLog);
        }
        return sendSuccess;
    }
	
	/** 
	 * @description: 调用email工程，发送邮件（多个附件）
	 * @author ZhangYuWei
	 * @throws Exception 
	 * @create 2019年05月16日
	 */
	@SuppressWarnings("unchecked")
	public Boolean sendEmailWithAttachments(String frommail,String bcc,String subject,
			String text,String tomail,List<Map<String,String>> attachments, Map<String,Object> emailUserNamePassword,Integer emailServiceType,Integer id){
    	logger.info("进入sendEmailWithAttachment方法，参数是：frommail={},bcc={},subject={},"
    			+ "tomail={},attachments={}",
    			frommail,bcc,subject,tomail,attachments);
    	ErrorEmailLog errorEmailLog = new ErrorEmailLog();//封装邮件日志
    	boolean sendSuccess = true;
    	String errorLog="";
        try {
        	if("test".equals(environmentType)) {
				frommail = testEmailFrommail;
				bcc = testEmailBcc;
				tomail = testEmailTomail;
				logger.info("测试环境......发件人：{},抄送人：{},收件人：{}",frommail,bcc,tomail);
			}
			logger.info("实际环境：{}",environmentType);
			
			//调用邮件管理工程，将发邮件送必要的参数传递过去，并发送多附件的邮件
			String emailUrl = emailServiceHost+"/nantian-erp/email/send/withAttachments";
			Map<String,String> emailParams = new HashMap<>();
			emailParams.put("frommail", frommail);
			emailParams.put("bcc", bcc);
			emailParams.put("subject", subject);
			emailParams.put("text", text);
			emailParams.put("tomail", tomail);
			emailParams.put("attachments", JSON.toJSONString(attachments));//多个附件的路径、名称
			emailParams.put("userName", emailUserNamePassword.get("userName") == null ? null : String.valueOf(emailUserNamePassword.get("userName")));
			emailParams.put("password", emailUserNamePassword.get("password") == null ? null : String.valueOf(emailUserNamePassword.get("password")));
			Map<String,String> headers = null;
			Map<String, String> emailResponse = HttpClientUtil.executePostMethodWithParas(emailUrl, JSON.toJSONString(emailParams), headers, "application/json", 30000);
			String code = emailResponse.get("code");//响应码
			String result = emailResponse.get("result");//响应结果
			logger.info("code={},result={}",code,result);
			StringBuilder attachmentPath = new StringBuilder();
			for(Map<String,String> attachment : attachments){
				attachmentPath.append(attachment.get("filepath")).append(",");
			}
			attachmentPath = attachmentPath.deleteCharAt(attachmentPath.length()-1);
			//发件人
			errorEmailLog.setSender(frommail);
			// 收件人
			errorEmailLog.setRecipient(tomail);
			//抄送人
			errorEmailLog.setBcc(bcc);
			//邮件主题
			errorEmailLog.setSubject(subject);
			// 邮件内容
			errorEmailLog.setEmailMessage(text);
			//附件路径
			errorEmailLog.setAttachmentPath(attachmentPath.toString());
			//邮件服务类型
			errorEmailLog.setEmailServiceType(emailServiceType);
			
			if(!"200".equals(code)) {
				logger.error("邮件工程响应失败！");
				sendSuccess = false;
				errorEmailLog.setType(0);//发送失败
				errorEmailLog.setErrorLog("邮件工程响应失败");// 错误日志
				return sendSuccess;
			}
			Map<String,Object> emailResultMap = (Map<String,Object>) JSON.parse(result);
			String statusOfResult = String.valueOf(emailResultMap.get("status"));//方法调用的返回码
			errorLog = String.valueOf(emailResultMap.get("data"));//方法调用的返回值
			if(!"200".equals(statusOfResult)) {
				logger.error("邮件发送失败！");
				sendSuccess = false;
				errorEmailLog.setType(0);//发送失败
				errorEmailLog.setErrorLog(errorLog);// 错误日志
				return sendSuccess;
			}
			errorEmailLog.setType(1);//发送成功
			errorEmailLog.setErrorLog(errorLog);
			errorEmailLog.setSendTime(new Date());
        } catch (Exception e) {
            logger.error("sendEmail异常："+e.getMessage(),e);
            sendSuccess = false;
        }finally{
        	saveEmailLog(id,sendSuccess,errorEmailLog);
        }
        return sendSuccess;
    }
	
	public void saveEmailLog(Integer id,boolean sendSuccess,ErrorEmailLog errorEmailLog){
		logger.info("saveEmailLog方法开始执行，参数id："+id+"参数：errorEmailLog"+errorEmailLog);
		try{
			if(id!=null){
				errorEmailLog.setId(id);
				errorEmailLog.setModifiedTime(new Date());
				this.errorEmailLogMapper.updateByPrimaryKeySelective(errorEmailLog);
			}else{
				//创建时间
				errorEmailLog.setCreateTime(new Date());
				//修改时间
				errorEmailLog.setModifiedTime(new Date());
				errorEmailLogMapper.insertSelective(errorEmailLog);
			}
		}catch(Exception e){
			logger.error("saveEmailLog方法发生异常",e.getMessage(),e);
		}
		
	}
}
