package com.nantian.erp.authentication.service;

import java.util.ArrayList;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 系统首页service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月27日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
@PropertySource(value= {"classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpHomePageService {
	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * Description: 首页-待办事项
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月27日 上午10:49:54
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findTodoList(String token){
		logger.info("进入findTodoList方法，参数是：token="+token);
		List<Map<String,Object>> todoList = new ArrayList<>();
		try {
			/*
			 * 调用人力资源工程查询所有的人力资源的待办事项
			 */
			String urlForHr = protocolType+"nantian-erp-hr/nantian-erp/hr/homePage/findTodoList";
			HttpHeaders requestHeadersForHr = new HttpHeaders();
			requestHeadersForHr.add("token", token);
		    HttpEntity<String> requestForHr = new HttpEntity<String>(null,requestHeadersForHr);
		    ResponseEntity<RestResponse> responseForHr = restTemplate.exchange(urlForHr,HttpMethod.GET,requestForHr,RestResponse.class);
		    //跨工程调用响应失败
		    if(200 != responseForHr.getStatusCodeValue() || !"200".equals(responseForHr.getBody().getStatus())) {
		    	logger.error("调用人力资源工程发生异常，响应失败！"+responseForHr);
		    	return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
			}
			//解析请求的结果
			List<Map<String,Object>> hrTodoList = (List<Map<String, Object>>) responseForHr.getBody().getData();
			
			/*
			 * 调用项目工程查询所有的关于项目的待办事项
			 */
			String urlForProject = protocolType+"nantian-erp-project/nantian-erp/project/homePage/findTodoList";
			HttpHeaders requestHeadersForProject = new HttpHeaders();
			requestHeadersForProject.add("token", token);
		    HttpEntity<String> requestForProject = new HttpEntity<String>(null,requestHeadersForProject);
		    ResponseEntity<RestResponse> responseForProject = restTemplate.exchange(urlForProject,HttpMethod.GET,requestForProject,RestResponse.class);
		    //跨工程调用响应失败
		    if(200 != responseForProject.getStatusCodeValue() || !"200".equals(responseForProject.getBody().getStatus())) {
		    	logger.error("调用项目工程发生异常，响应失败！"+responseForProject);
		    	return RestUtils.returnFailure("调用项目工程发生异常，响应失败！");
			}
			//解析请求的结果
			List<Map<String,Object>> projectTodoList = (List<Map<String, Object>>) responseForProject.getBody().getData();
			
			/*
			 * 调用薪酬工程查询所有的薪酬的待办事项
			 */
			String urlForSalary = protocolType+"nantian-erp-salary/nantian-erp/salary/homePage/findTodoList";
			HttpHeaders requestHeadersForSalary = new HttpHeaders();
			requestHeadersForSalary.add("token", token);
		    HttpEntity<String> requestForSalary = new HttpEntity<String>(null,requestHeadersForSalary);
		    ResponseEntity<RestResponse> responseForSalary = restTemplate.exchange(urlForSalary,HttpMethod.GET,requestForSalary,RestResponse.class);
		    //跨工程调用响应失败
		    if(200 != responseForSalary.getStatusCodeValue() || !"200".equals(responseForSalary.getBody().getStatus())) {
		    	logger.error("调用薪酬工程发生异常，响应失败！"+responseForSalary);
		    	return RestUtils.returnFailure("调用薪酬工程发生异常，响应失败！");
			}
			//解析请求的结果
		    List<Map<String, Object>> salaryTodoList = (List<Map<String, Object>>) responseForSalary.getBody().getData();
			
		    /*
		     * 将人力资源、薪酬、项目三部分的待办事项相加，返回给前端
		     */
		    todoList.addAll(hrTodoList);
		    todoList.addAll(projectTodoList);
		    todoList.addAll(salaryTodoList);
			return RestUtils.returnSuccess(todoList);
		} catch (Exception e) {
			logger.error("findTodoList方法发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致查询失败！");
		}
	}
	
}

