package com.nantian.erp.common.spring;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.nantian.erp.common.base.exception.BizException;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.constants.ExceptionConstants;

/**
 * 全局异常处理
 * <p>
 * @author 
 * @version 1.0 2019年3月30日
 * @see
 */
@ControllerAdvice
public class GlobalExceptionHandlingControllerAdvice {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${spring.application.name}")
	String name;

	/**
	 * 处理异常
	 * @param request
	 * @param response
	 * @param ex
	 * @throws Exception
	 */
	@ExceptionHandler(Exception.class)
	public void handleError(HttpServletRequest request,
			HttpServletResponse response, Exception ex) throws Exception {

		try {
			
			String exCode;
			String exMessage;

			if (ex instanceof BizException) {
				exCode = ((BizException) ex).getExCode();
				exMessage = ((BizException) ex).getExMessage();
			} else {
				exCode = ExceptionConstants.EX_INNER_ERROR;
				exMessage = ex.getMessage();
				if (StringUtils.isEmpty(exMessage)) {
					exMessage = ExceptionUtils.getFullStackTrace(ex);
				}
			}
			
			if(StringUtils.isEmpty(exMessage)){
				exMessage = ExceptionConstants.EX_INNER_ERROR_MSG;
			}
			
			String host = null;
			try {
				host = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				logger.error("get server host Exception e:", e);
			}
			if (StringUtils.isNotEmpty(host) && host.indexOf(".") != -1) {
				host = host.split("\\.")[3];
			}
			exMessage ="服务应用名称:" + name + ",主机IP地址：" + host + " " + exMessage;
			logger.error("系统发生异常,异常信息如下:");
			logger.error("异常编码:{}", exCode);
			logger.error("异常信息:{}", exMessage);
			logger.error("异常堆栈:", ex);
			
			response.setContentType("text/json;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			PrintWriter writer = response.getWriter();
			
		
			Map<String, String> retMsg = Maps.newHashMap();
			retMsg.put("exCode", exCode);
			retMsg.put("exMessage", exMessage);
		
			writer.write(JSON.toJSONString(RestUtils.returnFailure(retMsg)));
			writer.flush();
			writer.close();



		} catch (IOException e) {

			logger.error("全局异常处理失败:", ex);

		}

	}

}