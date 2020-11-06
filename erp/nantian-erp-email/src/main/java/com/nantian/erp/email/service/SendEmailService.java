/*
package com.nantian.erp.email.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.email.util.MailUtil;

*/
/**
 * Description: 发送邮件service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月08日      		ZhangYuWei          1.0       
 * </pre>
 *//*

@Service
public class SendEmailService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private MailUtil mailUtil;
	
	*/
/**
	 * Description: 发送不带附件的邮件
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月09日 上午09:35:28
	 *//*

	@Transactional
	public RestResponse withoutAttachment(Map<String,String> emailParams) {
		logger.info("withoutAttachment方法开始执行，参数是："+emailParams);
		try {
			String frommail = emailParams.get("frommail");//发件人
			String bcc = emailParams.get("bcc");//抄送人
			String subject = emailParams.get("subject");//主题
			String text = emailParams.get("text");//邮件内容
			String tomail = emailParams.get("tomail");//收件人

			String sendErrorMessage = mailUtil.sendMail(frommail, bcc, subject, text, tomail, null, null);
			if(sendErrorMessage != null) {
				return RestUtils.returnFailure(sendErrorMessage, "邮件发送失败！");
			}
			return RestUtils.returnSuccessWithString("邮件发送成功！");
		} catch (Exception e) {
			logger.info("withoutAttachment方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	*/
/**
	 * Description: 发送多附件的邮件
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月09日 上午10:51:46
	 *//*

	@SuppressWarnings("unchecked")
	@Transactional
	public RestResponse withAttachments(Map<String, String> emailParams) {
		logger.info("withAttachments方法开始执行，参数是："+emailParams);
		try {
			String frommail = emailParams.get("frommail");//发件人
			String bcc = emailParams.get("bcc");//抄送人
			String subject = emailParams.get("subject");//主题
			String text = emailParams.get("text");//邮件内容
			String tomail = emailParams.get("tomail");//收件人

			String userName = emailParams.get("userName");//账号
			String password = emailParams.get("password");//密码


			//List<Map<String,String>> attachments = (List<Map<String, String>>) emailParams.get("attachments");//多个附件的路径、名字
//			String attachmentsStr = emailParams.get("attachments");
//			JSON attachmentsJson = JSON.parseObject(attachmentsStr);
//			List<Map<String,String>> attachments = JSON.toJavaObject(attachmentsJson, List.class);//多个附件的路径、名字
			Object attachmentsObject = JSON.parse(emailParams.get("attachments"));
			List<Map<String,String>> attachmentsList = (List<Map<String,String>>) attachmentsObject;//多个附件的路径、名字
			String newUsername = null;
			String newPassword = null;
			if(userName != null && password != null){
				newUsername =  userName;
				newPassword = password;
			}

			String sendErrorMessage  = mailUtil.sendFileMailAttachments(frommail, bcc, subject, text, tomail, attachmentsList, newUsername, newPassword);
			if(sendErrorMessage != null) {
				return RestUtils.returnFailure(sendErrorMessage, "邮件发送失败！");
			}
			return RestUtils.returnSuccessWithString("邮件发送成功！");
		} catch (Exception e) {
			logger.info("withAttachments方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	*/
/**
	 * Description: 发送单附件的邮件
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年05月10日
	 *//*

	@Transactional
	public RestResponse withAttachment(Map<String, String> emailParams) {
		logger.info("withAttachment方法开始执行，参数是："+emailParams);
		try {
			String frommail = emailParams.get("frommail");//发件人
			String bcc = emailParams.get("bcc");//抄送人
			String subject = emailParams.get("subject");//主题
			String text = emailParams.get("text");//邮件内容
			String tomail = emailParams.get("tomail");//收件人
			String filename = emailParams.get("filename");//附件名字
			String filepath = emailParams.get("filepath");//附件路径

			String sendErrorMessage = mailUtil.sendFileMail(frommail, bcc, subject, text, tomail, filename, filepath, null, null);
			if(sendErrorMessage != null) {
				return RestUtils.returnFailure(sendErrorMessage, "邮件发送失败！");
			}
			return RestUtils.returnSuccessWithString("邮件发送成功！");
		} catch (Exception e) {
			logger.info("withAttachment方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
}
*/
