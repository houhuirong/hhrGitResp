package com.nantian.erp.salary.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * @description: 调用hr工程，根据员工ID查询员工姓名
	 * @author ZhangYuWei
	 * @create 2019年04月10日
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String findEmpNameByEmployeeId(String token,Integer employeeId) {
    	logger.info("进入findEmpNameByEmployeeId方法，参数是：token={},employeeId={}",token,employeeId);
    	try {
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeDetail?employeeId="+employeeId;
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
		    HttpEntity<String> requestEntity=new HttpEntity<>(null,requestHeaders);
		    ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.GET,requestEntity,Map.class);
		    logger.info("跨工程调用的响应结果response={}",response);
		    Map<String,Object> resultMap = response.getBody();
		    if(response.getStatusCodeValue() != 200 || resultMap.get("data")==null || "".equals(resultMap.get("data"))){
		    	logger.error("人力资源工程响应失败！");
		    	return null;
		    }
		    Map<String,Object> employeeMap = (Map<String, Object>) resultMap.get("data");
		    if(employeeMap==null || employeeMap.get("name")==null){
		    	logger.error("通过用户ID未获取到用户信息");
		    	return null;
		    }
		    return String.valueOf(employeeMap.get("name"));//获取员工姓名
        } catch (Exception e) {
            logger.error("findUsernameByEmployeeId："+e.getMessage(),e);
            return null;
        }
    }
	
	/** 
	 * @description: 调用hr工程，查询员工表信息（参数可以为空）
	 * 定时任务调用时，token可以为null
	 * @author ZhangYuWei
	 * @create 2019年04月03日
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map<String,Object>> findEmployeeTable(String token,Map<String,Object> requestBody) {
    	logger.info("进入findEmployeeTable方法，参数是：token={},requestBody={}",token,requestBody);
    	List<Map<String,Object>> employeeList = new ArrayList<>();
        try {
        	//调用人力资源工程，查询单表基本员工信息
        	String url = "";
        	if(token==null) {
        		url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeTableForScheduler";
        	}else {
        		url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeTable";
        	}
        	
        	HttpHeaders requestHeaders = new HttpHeaders();
        	requestHeaders.add("token",token);//将token放到请求头中
        	HttpEntity<Map<String,Object>> request = new HttpEntity<>(requestBody, requestHeaders);
        	
        	ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
        	if(response.getStatusCodeValue()==200 && "200".equals(response.getBody().get("status"))){
        		employeeList = (List<Map<String, Object>>) response.getBody().get("data");
        	}
        } catch (Exception e) {
            logger.error("findEmployeeTable异常：",e.getMessage(),e);
        }
        return employeeList;
    }
	
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
	 * 跨工程调用get方法，其中参数url需要将参数拼进去
	 * 返回的数据为resultMap内的data
	 * @param token
	 * @param url
	 * @return
	 * @author hehui
	 */
	public Map<String,Object> getRequestMethod(String token,String url) {
    	logger.info("进入跨工程调用get接口的方法，参数是：token={},url={}",token,url);
    	HttpHeaders requestHeaders=new HttpHeaders();
    	requestHeaders.add("token", token);
    	HttpEntity<String> requestEntity=new HttpEntity<>(null,requestHeaders);
    	ResponseEntity<Map> response = restTemplate.exchange(protocolType+url,HttpMethod.GET,requestEntity,Map.class);
    	logger.info("跨工程调用的响应结果response={}",response);
    	Map<String,Object> resultMap = response.getBody();
    	if(response.getStatusCodeValue() != 200 || resultMap.get("data")==null || "".equals(resultMap.get("data"))){
    		logger.error(url+"响应失败！");
    		return null;
    	}
    	return resultMap;
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
}
